package pt.ulisboa.tecnico.rnl.dei.ems.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.CreateReviewDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.GradeSummaryDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.QuestionCropRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.ReviewRequestDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.SubmitEvaluationDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.SubmitReviewDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.TeacherTaskDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ExamRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.QuestionRepository;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.EvaluationService;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.ExamService;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.GradeService;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.ReviewService;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.ems.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.DisciplineRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmsFullWorkflowTests {

	@Autowired
	private ExamService examService;

	@Autowired
	private EvaluationService evaluationService;

	@Autowired
	private GradeService gradeService;

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private ExamRepository examRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private DisciplineRepository disciplineRepository;


	@Test
	public void testFullWorkflowFromDistributionToReview() {
		// 1. Arrange seeded exam
		Exam seededExam = examRepository.findAll().get(0);
		Discipline matA = disciplineRepository.findByCode("MAT-A").orElseThrow();
		Person student = seededExam.getStudent();
		Person teacherMat = personRepository.findByEmail("prof.mat@dei.tecnico.ulisboa.pt").orElseThrow();

		// Add two questions and complete segmentation
		examService.addQuestion(seededExam.getId(), new QuestionCropRequest("1.1", 8.0, 1, 0.1, 0.1, 0.8, 0.3, matA.getId()));
		examService.addQuestion(seededExam.getId(), new QuestionCropRequest("1.2", 12.0, 1, 0.1, 0.4, 0.8, 0.3, matA.getId()));
		examService.completeSegmentation(seededExam.getId());

		Exam segmentedExam = examRepository.findById(seededExam.getId()).orElseThrow();
		assertEquals(ExamStatus.SEGMENTED, segmentedExam.getStatus());
		assertEquals(20.0, segmentedExam.getTotalScore());

		// 2. Distribution
		Map<String, Object> distResult = evaluationService.distributeSegmentedExams(matA.getId(), null);
		assertTrue((int) distResult.get("distributedQuestionsCount") >= 2);

		Exam distributedExam = examRepository.findById(seededExam.getId()).orElseThrow();
		assertEquals(ExamStatus.DISTRIBUTED, distributedExam.getStatus());

		// 3. Teacher Task Evaluation
		List<Question> examQuestions = questionRepository.findByExamIdOrderByOrderIndexAsc(seededExam.getId());
		for (Question q : examQuestions) {
			assertNotNull(q.getEvaluator(), "Question should have an assigned evaluator");
			double scoreToGive = q.getQuestionNumber().equals("1.1") ? 7.0 : 11.0;
			evaluationService.submitTaskEvaluation(q.getEvaluator().getId(), q.getId(), new SubmitEvaluationDto(scoreToGive, "Bom raciocinio"));
		}

		Exam correctedExam = examRepository.findById(seededExam.getId()).orElseThrow();
		assertEquals(ExamStatus.CORRECTED, correctedExam.getStatus());
		assertEquals(18.0, correctedExam.getObtainedScore());


		// 4. Pauta and Bulk Release
		List<GradeSummaryDto> grades = gradeService.getGrades(null, matA.getId());
		assertFalse(grades.isEmpty());

		gradeService.bulkRelease(null, matA.getId());
		Exam releasedExam = examRepository.findById(seededExam.getId()).orElseThrow();
		assertEquals(ExamStatus.RELEASED, releasedExam.getStatus());
		assertTrue(releasedExam.isReleased());
		assertNotNull(releasedExam.getReviewDeadline());

		// 5. Student Review Request
		Question q1 = releasedExam.getQuestions().get(0);
		ReviewRequestDto reviewDto = reviewService.createReviewRequest(student.getId(), releasedExam.getId(),
				new CreateReviewDto(q1.getId(), "Discordo do desconto aplicado no passo 2"));
		assertNotNull(reviewDto.id());
		assertEquals("PENDING", reviewDto.status());

		// 6. Close Timeline & Distribute Reviews
		Map<String, Object> revDistResult = reviewService.closeTimelineAndDistributeReviews(matA.getId());
		assertTrue((int) revDistResult.get("distributedReviewsCount") >= 1);

		// Get the created review from DB to check who was assigned
		ReviewRequestDto assignedReview = reviewService.getStudentReviews(student.getId()).get(0);
		assertNotNull(assignedReview.reviewerId(), "Review should be assigned to an independent reviewer");

		List<ReviewRequestDto> teacherReviewTasks = reviewService.getTeacherReviewTasks(assignedReview.reviewerId());
		assertFalse(teacherReviewTasks.isEmpty());

		// 7. Teacher Evaluation of Review Request
		ReviewRequestDto reviewToEvaluate = teacherReviewTasks.stream()
				.filter(r -> r.questionId().equals(q1.getId()))
				.findFirst()
				.orElseThrow();

		ReviewRequestDto resolvedReview = reviewService.submitReviewEvaluation(
				assignedReview.reviewerId(),
				reviewToEvaluate.id(),
				new SubmitReviewDto(8.0, "Revisto com sucesso, cotacao maxima atribuida")
		);

		assertEquals("RESOLVED", resolvedReview.status());
		assertEquals(8.0, resolvedReview.revisedScore());

		Exam finalExam = examRepository.findById(seededExam.getId()).orElseThrow();
		assertEquals(19.0, finalExam.getObtainedScore());
	}
}

