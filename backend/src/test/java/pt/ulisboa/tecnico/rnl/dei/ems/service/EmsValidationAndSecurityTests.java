package pt.ulisboa.tecnico.rnl.dei.ems.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.CreateReviewDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.GradeSummaryDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.QuestionCropRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.SubmitEvaluationDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ExamRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.QuestionRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.EvaluationService;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.ExamService;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.GradeService;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.ReviewService;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.School;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.DisciplineRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EmsValidationAndSecurityTests {

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
	public void testInvalidScoreSubmissions() {
		Exam exam = examRepository.findAll().get(0);
		Discipline matA = disciplineRepository.findByCode("MAT-A").orElseThrow();
		Person teacherMat = personRepository.findByEmail("prof.mat@dei.tecnico.ulisboa.pt").orElseThrow();

		examService.addQuestion(exam.getId(), new QuestionCropRequest("1.1", 20.0, 1, 0.1, 0.1, 0.8, 0.3, matA.getId()));
		
		// Cannot add another question that exceeds 20.0
		assertThrows(DEIException.class, () -> {
			examService.addQuestion(exam.getId(), new QuestionCropRequest("1.2", 1.0, 1, 0.1, 0.1, 0.8, 0.3, matA.getId()));
		});

		examService.completeSegmentation(exam.getId());
		evaluationService.distributeSegmentedExams(matA.getId(), null);

		Question q = questionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId()).get(0);
		Long assignedTeacherId = q.getEvaluator().getId();

		// Cannot assign negative score
		assertThrows(DEIException.class, () -> {
			evaluationService.submitTaskEvaluation(assignedTeacherId, q.getId(), new SubmitEvaluationDto(-1.0, "Nota negativa"));
		});

		// Cannot assign score higher than maxScore (20.0)
		assertThrows(DEIException.class, () -> {
			evaluationService.submitTaskEvaluation(assignedTeacherId, q.getId(), new SubmitEvaluationDto(20.5, "Nota superior ao maximo"));
		});
	}

	@Test
	public void testEvaluationWithCanvasAnnotation() {
		Exam exam = examRepository.findAll().get(0);
		Discipline matA = disciplineRepository.findByCode("MAT-A").orElseThrow();

		examService.addQuestion(exam.getId(), new QuestionCropRequest("1.1", 20.0, 1, 0.1, 0.1, 0.8, 0.3, matA.getId()));
		examService.completeSegmentation(exam.getId());

		evaluationService.distributeSegmentedExams(matA.getId(), null);

		Question q = questionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId()).get(0);
		Long assignedTeacherId = q.getEvaluator().getId();

		// Submit with a mock 1x1 PNG base64 string
		String mockBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
		var taskDto = evaluationService.submitTaskEvaluation(
				assignedTeacherId,
				q.getId(),
				new SubmitEvaluationDto(18.5, "Boa resolução com anotações", mockBase64)
		);

		assertTrue(taskDto.hasAnnotation());
		Question updatedQ = questionRepository.findById(q.getId()).orElseThrow();
		assertNotNull(updatedQ.getAnnotatedImagePath());

		var resource = examService.getQuestionAnnotatedImageResource(q.getId());
		assertNotNull(resource);
		assertTrue(resource.exists());
	}

	@Test
	public void testStaffCannotEditOrCreateExamsAfterDistribution() {
		Exam exam = examRepository.findAll().get(0);
		Discipline matA = disciplineRepository.findByCode("MAT-A").orElseThrow();
		Person student = exam.getStudent();
		School school = exam.getSchool();

		examService.addQuestion(exam.getId(), new QuestionCropRequest("1.1", 20.0, 1, 0.1, 0.1, 0.8, 0.3, matA.getId()));
		examService.completeSegmentation(exam.getId());

		// Distribute exams
		evaluationService.distributeSegmentedExams(matA.getId(), null);

		assertTrue(examService.areSubmissionsLocked());

		// 1. Staff cannot create new exams after distribution
		org.springframework.mock.web.MockMultipartFile dummyPdf = new org.springframework.mock.web.MockMultipartFile(
				"file", "new_exam.pdf", "application/pdf", new byte[]{1, 2, 3}
		);
		assertThrows(DEIException.class, () -> {
			examService.createExam(dummyPdf, "Exame Bloqueado", school.getId(), matA.getId(), student.getId());
		});

		// 2. Staff cannot modify questions on distributed exam
		assertThrows(DEIException.class, () -> {
			examService.addQuestion(exam.getId(), new QuestionCropRequest("2.1", 5.0, 1, 0.1, 0.1, 0.5, 0.5, matA.getId()));
		});

		// 3. Staff cannot delete exam after distribution
		assertThrows(DEIException.class, () -> {
			examService.deleteExam(exam.getId());
		});
	}

	@Test
	public void testCannotCreateDuplicateExamForSameStudentAndDiscipline() {
		Exam existingExam = examRepository.findAll().get(0);
		Person student = existingExam.getStudent();
		School school = existingExam.getSchool();
		Discipline discipline = existingExam.getDiscipline();

		org.springframework.mock.web.MockMultipartFile dummyPdf = new org.springframework.mock.web.MockMultipartFile(
				"file", "duplicate_exam.pdf", "application/pdf", new byte[]{1, 2, 3}
		);

		DEIException ex = assertThrows(DEIException.class, () -> {
			examService.createExam(dummyPdf, "Exame Duplicado", school.getId(), discipline.getId(), student.getId());
		});
		assertEquals(pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage.STUDENT_ALREADY_HAS_EXAM_FOR_DISCIPLINE, ex.getErrorMessage());
	}

	@Test
	public void testRolePermissionsDistributionAndRelease() {
		var staffPerms = pt.ulisboa.tecnico.rnl.dei.ems.security.RolePermissions.forRole(pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType.SCHOOL_STAFF);
		var adminPerms = pt.ulisboa.tecnico.rnl.dei.ems.security.RolePermissions.forRole(pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType.ADMINISTRATOR);

		// Staff cannot distribute exams
		assertFalse(staffPerms.contains(pt.ulisboa.tecnico.rnl.dei.ems.security.Permission.EXAM_DISTRIBUTE));

		// Admin can distribute exams
		assertTrue(adminPerms.contains(pt.ulisboa.tecnico.rnl.dei.ems.security.Permission.EXAM_DISTRIBUTE));

		// Staff can release exams and view grades
		assertTrue(staffPerms.contains(pt.ulisboa.tecnico.rnl.dei.ems.security.Permission.EXAM_RELEASE));
		assertTrue(staffPerms.contains(pt.ulisboa.tecnico.rnl.dei.ems.security.Permission.GRADES_READ));
	}

	@Test
	public void testStudentRequestExamViewAndStaffApprove() {
		Exam exam = examRepository.findAll().get(0);
		exam.setStatus(Exam.ExamStatus.CORRECTED);
		examRepository.save(exam);
		assertFalse(exam.isViewRequested());
		assertFalse(exam.isReleased());

		// Student requests viewing
		var examDto = gradeService.requestExamView(exam.getId());
		assertTrue(examDto.viewRequested());

		Exam updated = examRepository.findById(exam.getId()).orElseThrow();
		assertTrue(updated.isViewRequested());

		// Staff approves viewing
		var releasedDto = gradeService.releaseExam(exam.getId());
		assertTrue(releasedDto.released());
		assertFalse(releasedDto.viewRequested());
	}

	@Test
	public void testPublishInitialGradesStarts48hWindow() {
		Exam exam = examRepository.findAll().get(0);
		exam.setStatus(Exam.ExamStatus.CORRECTED);
		examRepository.save(exam);
		assertFalse(exam.isGradesPublished());

		var grades = gradeService.publishInitialGrades(null, exam.getDiscipline().getId());
		assertFalse(grades.isEmpty());
		assertTrue(grades.get(0).gradesPublished());
		assertNotNull(grades.get(0).reviewDeadline());
	}

	@Test
	public void testCannotReleaseExamIfNotCorrected() {
		Exam exam = examRepository.findAll().get(0);
		assertEquals(Exam.ExamStatus.UPLOADED, exam.getStatus());

		DEIException ex = assertThrows(DEIException.class, () -> {
			gradeService.releaseExam(exam.getId());
		});
		assertEquals(pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage.EXAM_NOT_CORRECTED, ex.getErrorMessage());
	}

	@Test
	public void testCannotDistributeIfUnsegmentedExamsExist() {
		Exam exam = examRepository.findAll().get(0);
		assertEquals(Exam.ExamStatus.UPLOADED, exam.getStatus());
		Discipline matA = exam.getDiscipline();

		// Trying to distribute when the exam is still in UPLOADED (not segmented) status throws exception
		DEIException ex = assertThrows(DEIException.class, () -> {
			evaluationService.distributeSegmentedExams(matA.getId(), null);
		});
		assertEquals(pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage.CANNOT_DISTRIBUTE_UNSEGMENTED_EXAMS_EXIST, ex.getErrorMessage());
	}

	@Test
	public void testCannotPublishReviewGradesWhileReviewsPending() {
		Exam exam = examRepository.findAll().get(0);
		Discipline matA = exam.getDiscipline();
		Person student = exam.getStudent();

		// Segment, distribute, evaluate and release exam
		examService.addQuestion(exam.getId(), new QuestionCropRequest("1.1", 20.0, 1, 0.1, 0.1, 0.8, 0.3, matA.getId()));
		examService.completeSegmentation(exam.getId());
		evaluationService.distributeSegmentedExams(matA.getId(), null);

		Question q = questionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId()).get(0);
		evaluationService.submitTaskEvaluation(q.getEvaluator().getId(), q.getId(), new SubmitEvaluationDto(15.0, "Bom"));
		gradeService.bulkRelease(null, matA.getId());

		// Student requests review -> question enters IN_REVIEW
		reviewService.createReviewRequest(student.getId(), exam.getId(), new CreateReviewDto(q.getId(), "Pretendo revisao"));

		// Staff tries to publish review grades while review is pending evaluation
		DEIException ex = assertThrows(DEIException.class, () -> {
			gradeService.publishReviewGrades(null, matA.getId());
		});
		assertEquals(pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage.REVIEWS_STILL_PENDING, ex.getErrorMessage());
	}

	@Test
	public void testStaffCannotSeeObtainedScoreBeforePublication() {
		Exam exam = examRepository.findAll().get(0);
		Discipline matA = exam.getDiscipline();

		examService.addQuestion(exam.getId(), new QuestionCropRequest("1.1", 20.0, 1, 0.1, 0.1, 0.8, 0.3, matA.getId()));
		examService.completeSegmentation(exam.getId());
		evaluationService.distributeSegmentedExams(matA.getId(), null);

		Question q = questionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId()).get(0);
		evaluationService.submitTaskEvaluation(q.getEvaluator().getId(), q.getId(), new SubmitEvaluationDto(16.5, "Excelente"));

		// Exam is now in CORRECTED state, but grades have not been published
		Exam correctedExam = examRepository.findById(exam.getId()).orElseThrow();
		assertEquals(Exam.ExamStatus.CORRECTED, correctedExam.getStatus());
		assertFalse(correctedExam.isGradesPublished());
		assertFalse(correctedExam.isReleased());

		// In getGrades (pauta), obtainedScore must be null before publication
		List<GradeSummaryDto> pautaBeforePublish = gradeService.getGrades(null, matA.getId());
		assertEquals(1, pautaBeforePublish.size());
		assertNull(pautaBeforePublish.get(0).obtainedScore(), "Staff must not have access to obtainedScore before publication");

		// After publishing initial grades, obtainedScore becomes available
		List<GradeSummaryDto> pautaAfterPublish = gradeService.publishInitialGrades(null, matA.getId());
		assertEquals(1, pautaAfterPublish.size());
		assertNotNull(pautaAfterPublish.get(0).obtainedScore(), "Obtained score should be visible once grades are published");
		assertEquals(16.5, pautaAfterPublish.get(0).obtainedScore());
	}
}
