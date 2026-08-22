package pt.ulisboa.tecnico.rnl.dei.ems.exam.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question.QuestionStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.ReviewRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.ReviewRequest.ReviewStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.CreateReviewDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.ReviewRequestDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.SubmitReviewDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ExamRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.QuestionRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ReviewRequestRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.ems.person.repository.PersonRepository;

@Service
@Transactional
public class ReviewService {

	private final ReviewRequestRepository reviewRequestRepository;
	private final ExamRepository examRepository;
	private final QuestionRepository questionRepository;
	private final PersonRepository personRepository;

	public ReviewService(
			ReviewRequestRepository reviewRequestRepository,
			ExamRepository examRepository,
			QuestionRepository questionRepository,
			PersonRepository personRepository) {
		this.reviewRequestRepository = reviewRequestRepository;
		this.examRepository = examRepository;
		this.questionRepository = questionRepository;
		this.personRepository = personRepository;
	}

	public ReviewRequestDto createReviewRequest(long studentId, long examId, CreateReviewDto dto) {
		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_EXAM, Long.toString(examId)));

		if (exam.getStudent() == null || !exam.getStudent().getId().equals(studentId)) {
			throw new DEIException(ErrorMessage.ACCESS_DENIED);
		}

		if (!exam.isReleased()) {
			throw new DEIException(ErrorMessage.EXAM_NOT_RELEASED);
		}

		if (exam.getReviewDeadline() != null && LocalDateTime.now().isAfter(exam.getReviewDeadline())) {
			throw new DEIException(ErrorMessage.REVIEW_DEADLINE_EXPIRED);
		}

		if (dto.questionId() == null) {
			throw new DEIException(ErrorMessage.NO_SUCH_QUESTION, "null");
		}

		Question question = questionRepository.findById(dto.questionId())
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(dto.questionId())));

		if (!question.getExam().getId().equals(exam.getId())) {
			throw new DEIException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(dto.questionId()));
		}

		if (dto.justification() == null || dto.justification().trim().isBlank()) {
			throw new DEIException(ErrorMessage.REVIEW_JUSTIFICATION_REQUIRED);
		}

		if (reviewRequestRepository.findByQuestionIdAndStudentId(question.getId(), studentId).isPresent()) {
			throw new DEIException(ErrorMessage.REVIEW_ALREADY_REQUESTED);
		}

		Person student = exam.getStudent();
		ReviewRequest reviewRequest = new ReviewRequest(question, student, dto.justification().trim(), question.getScore());
		question.setStatus(QuestionStatus.IN_REVIEW);
		questionRepository.save(question);

		return new ReviewRequestDto(reviewRequestRepository.save(reviewRequest));
	}

	@Transactional(readOnly = true)
	public List<ReviewRequestDto> getStudentReviews(long studentId) {
		return reviewRequestRepository.findByStudentId(studentId).stream()
				.map(ReviewRequestDto::new)
				.toList();
	}

	@Scheduled(fixedRate = 10000)
	public void autoCloseExpiredTimelinesAndDistribute() {
		List<ReviewRequest> pendingReviews = reviewRequestRepository.findByStatus(ReviewStatus.PENDING);
		if (pendingReviews.isEmpty()) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		List<ReviewRequest> expiredReviews = pendingReviews.stream()
				.filter(r -> {
					Exam exam = r.getQuestion().getExam();
					return exam != null && exam.getReviewDeadline() != null && now.isAfter(exam.getReviewDeadline());
				})
				.toList();

		for (ReviewRequest req : expiredReviews) {
			try {
				distributeSingleReview(req);
			} catch (Exception e) {
				// Continue with next review if any exception occurs
			}
		}
	}

	public Map<String, Object> closeTimelineAndDistributeReviews(Long disciplineId) {
		List<ReviewRequest> pendingReviews = reviewRequestRepository.findByStatus(ReviewStatus.PENDING);
		if (disciplineId != null) {
			pendingReviews = pendingReviews.stream()
					.filter(r -> r.getQuestion().getDiscipline().getId().equals(disciplineId))
					.toList();
		}

		if (pendingReviews.isEmpty()) {
			Map<String, Object> res = new HashMap<>();
			res.put("distributedReviewsCount", 0);
			return res;
		}

		int distributedCount = 0;
		for (ReviewRequest req : pendingReviews) {
			if (distributeSingleReview(req)) {
				distributedCount++;
			}
		}

		Map<String, Object> res = new HashMap<>();
		res.put("distributedReviewsCount", distributedCount);
		return res;
	}

	private boolean distributeSingleReview(ReviewRequest req) {
		Long discId = req.getQuestion().getDiscipline().getId();
		List<Person> teachers = personRepository.findByTypeAndDisciplinesId(PersonType.TEACHER, discId);

		if (teachers.isEmpty()) {
			throw new DEIException(ErrorMessage.NO_TEACHERS_FOR_DISCIPLINE, req.getQuestion().getDiscipline().getName());
		}

		// Prioritize a teacher different from the original evaluator if available
		Person originalEvaluator = req.getQuestion().getEvaluator();
		List<Person> candidateTeachers = teachers;
		if (teachers.size() > 1 && originalEvaluator != null) {
			List<Person> otherTeachers = teachers.stream()
					.filter(t -> !t.getId().equals(originalEvaluator.getId()))
					.toList();
			if (!otherTeachers.isEmpty()) {
				candidateTeachers = otherTeachers;
			}
		}

		// Assign to the teacher with the least active review workload
		Person selectedTeacher = candidateTeachers.stream()
				.min(Comparator.comparingLong(t -> reviewRequestRepository.countByReviewerIdAndStatus(t.getId(), ReviewStatus.ASSIGNED)))
				.orElse(candidateTeachers.get(0));

		req.setReviewer(selectedTeacher);
		req.setStatus(ReviewStatus.ASSIGNED);
		reviewRequestRepository.save(req);
		return true;
	}

	public List<ReviewRequestDto> getTeacherReviewTasks(long teacherId) {
		autoCloseExpiredTimelinesAndDistribute();
		return reviewRequestRepository.findByReviewerId(teacherId).stream()
				.map(ReviewRequestDto::new)
				.toList();
	}

	public ReviewRequestDto submitReviewEvaluation(long teacherId, long reviewId, SubmitReviewDto dto) {
		ReviewRequest review = reviewRequestRepository.findById(reviewId)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_REVIEW_REQUEST, Long.toString(reviewId)));

		if (review.getReviewer() == null || !review.getReviewer().getId().equals(teacherId)) {
			throw new DEIException(ErrorMessage.REVIEW_NOT_ASSIGNED_TO_TEACHER);
		}

		Question question = review.getQuestion();
		if (dto.revisedScore() == null || dto.revisedScore() < 0 || dto.revisedScore() > question.getMaxScore()) {
			throw new DEIException(ErrorMessage.INVALID_SCORE_RANGE, String.valueOf(question.getMaxScore()));
		}

		review.setRevisedScore(dto.revisedScore());
		review.setReviewerFeedback(dto.reviewerFeedback() != null ? dto.reviewerFeedback().trim() : null);
		review.setStatus(ReviewStatus.RESOLVED);
		review.setResolvedAt(LocalDateTime.now());
		reviewRequestRepository.save(review);

		question.setScore(dto.revisedScore());
		question.setStatus(QuestionStatus.REVIEWED);
		questionRepository.save(question);

		Exam exam = question.getExam();
		if (exam != null) {
			exam.calculateObtainedScore();
			examRepository.save(exam);
		}

		return new ReviewRequestDto(review);
	}
}
