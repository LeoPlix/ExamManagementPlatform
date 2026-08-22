package pt.ulisboa.tecnico.rnl.dei.ems.exam.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.ExamDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.GradeSummaryDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ExamRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.ems.security.SecurityUtils;

@Service
@Transactional
public class GradeService {

	private final ExamRepository examRepository;

	public GradeService(ExamRepository examRepository) {
		this.examRepository = examRepository;
	}

	@Transactional(readOnly = true)
	public List<GradeSummaryDto> getGrades(Long schoolId, Long disciplineId) {
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.SCHOOL_STAFF) {
			if (current.getSchool() != null) {
				schoolId = current.getSchool().getId();
			}
		}

		List<Exam> exams;
		if (schoolId != null && disciplineId != null) {
			exams = examRepository.findBySchoolIdAndDisciplineId(schoolId, disciplineId);
		} else if (schoolId != null) {
			exams = examRepository.findBySchoolId(schoolId);
		} else if (disciplineId != null) {
			exams = examRepository.findByDisciplineId(disciplineId);
		} else {
			exams = examRepository.findAll();
		}

		return exams.stream()
				.map(GradeSummaryDto::new)
				.toList();
	}

	public List<GradeSummaryDto> publishInitialGrades(Long schoolId, Long disciplineId) {
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.SCHOOL_STAFF) {
			if (current.getSchool() != null) {
				schoolId = current.getSchool().getId();
			}
		}

		List<Exam> candidateExams;
		if (schoolId != null && disciplineId != null) {
			candidateExams = examRepository.findBySchoolIdAndDisciplineId(schoolId, disciplineId);
		} else if (schoolId != null) {
			candidateExams = examRepository.findBySchoolId(schoolId);
		} else if (disciplineId != null) {
			candidateExams = examRepository.findByDisciplineId(disciplineId);
		} else {
			candidateExams = examRepository.findAll();
		}

		if (candidateExams.isEmpty()) {
			return List.of();
		}

		// Enforce: ALL candidate exams must be fully evaluated (CORRECTED or RELEASED)
		boolean anyUncorrected = candidateExams.stream()
				.anyMatch(e -> e.getStatus() != ExamStatus.CORRECTED && e.getStatus() != ExamStatus.RELEASED);

		if (anyUncorrected) {
			throw new DEIException(ErrorMessage.EXAM_NOT_CORRECTED);
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime deadline = now.plusHours(48);

		for (Exam exam : candidateExams) {
			exam.calculateObtainedScore();
			exam.setGradesPublished(true);
			if (exam.getGradesPublishedAt() == null) {
				exam.setGradesPublishedAt(now);
			}
			if (exam.getReviewDeadline() == null) {
				exam.setReviewDeadline(deadline);
			}
			examRepository.save(exam);
		}

		return candidateExams.stream()
				.map(GradeSummaryDto::new)
				.toList();
	}

	public GradeSummaryDto releaseExam(long examId) {
		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_EXAM, Long.toString(examId)));

		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.SCHOOL_STAFF) {
			if (current.getSchool() != null && exam.getSchool() != null
					&& !current.getSchool().getId().equals(exam.getSchool().getId())) {
				throw new DEIException(ErrorMessage.ACCESS_DENIED);
			}
		}

		if (exam.getStatus() != ExamStatus.CORRECTED && exam.getStatus() != ExamStatus.RELEASED) {
			throw new DEIException(ErrorMessage.EXAM_NOT_CORRECTED);
		}

		LocalDateTime now = LocalDateTime.now();
		exam.calculateObtainedScore();
		exam.setReleased(true);
		exam.setStatus(ExamStatus.RELEASED);
		exam.setReleasedAt(now);
		exam.setViewRequested(false);
		if (!exam.isGradesPublished()) {
			exam.setGradesPublished(true);
			exam.setGradesPublishedAt(now);
		}
		if (exam.getReviewDeadline() == null) {
			exam.setReviewDeadline(now.plusHours(48));
		}
		examRepository.save(exam);

		return new GradeSummaryDto(exam);
	}

	public ExamDto requestExamView(long examId) {
		Exam exam = examRepository.findById(examId)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_EXAM, Long.toString(examId)));

		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.STUDENT) {
			if (exam.getStudent() == null || !exam.getStudent().getId().equals(current.getId())) {
				throw new DEIException(ErrorMessage.ACCESS_DENIED);
			}
		}

		exam.setViewRequested(true);
		exam.setViewRequestedAt(LocalDateTime.now());
		examRepository.save(exam);

		return new ExamDto(exam);
	}

	public List<GradeSummaryDto> bulkRelease(Long schoolId, Long disciplineId) {
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.SCHOOL_STAFF) {
			if (current.getSchool() != null) {
				schoolId = current.getSchool().getId();
			}
		}

		List<Exam> candidateExams;
		if (schoolId != null && disciplineId != null) {
			candidateExams = examRepository.findBySchoolIdAndDisciplineId(schoolId, disciplineId);
		} else if (schoolId != null) {
			candidateExams = examRepository.findBySchoolId(schoolId);
		} else if (disciplineId != null) {
			candidateExams = examRepository.findByDisciplineId(disciplineId);
		} else {
			candidateExams = examRepository.findAll();
		}

		List<Exam> unreleasedExams = candidateExams.stream()
				.filter(e -> !e.isReleased())
				.toList();

		if (unreleasedExams.isEmpty()) {
			return List.of();
		}

		// Enforce: ALL candidate exams must be fully evaluated (CORRECTED or RELEASED)
		boolean anyUncorrected = candidateExams.stream()
				.anyMatch(e -> e.getStatus() != ExamStatus.CORRECTED && e.getStatus() != ExamStatus.RELEASED);

		if (anyUncorrected) {
			throw new DEIException(ErrorMessage.EXAM_NOT_CORRECTED);
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime deadline = now.plusHours(48);

		for (Exam exam : unreleasedExams) {
			exam.calculateObtainedScore();
			exam.setReleased(true);
			exam.setStatus(ExamStatus.RELEASED);
			exam.setReleasedAt(now);
			exam.setViewRequested(false);
			if (!exam.isGradesPublished()) {
				exam.setGradesPublished(true);
				exam.setGradesPublishedAt(now);
			}
			if (exam.getReviewDeadline() == null) {
				exam.setReviewDeadline(deadline);
			}
			examRepository.save(exam);
		}

		return unreleasedExams.stream()
				.map(GradeSummaryDto::new)
				.toList();
	}

	public List<GradeSummaryDto> publishReviewGrades(Long schoolId, Long disciplineId) {
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.SCHOOL_STAFF) {
			if (current.getSchool() != null) {
				schoolId = current.getSchool().getId();
			}
		}

		List<Exam> candidateExams;
		if (schoolId != null && disciplineId != null) {
			candidateExams = examRepository.findBySchoolIdAndDisciplineId(schoolId, disciplineId);
		} else if (schoolId != null) {
			candidateExams = examRepository.findBySchoolId(schoolId);
		} else if (disciplineId != null) {
			candidateExams = examRepository.findByDisciplineId(disciplineId);
		} else {
			candidateExams = examRepository.findAll();
		}

		// Enforce that no reviews are still pending evaluation by teachers
		long pendingReviewsCount = candidateExams.stream()
				.flatMap(e -> e.getQuestions().stream())
				.filter(q -> q.getStatus() == pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question.QuestionStatus.IN_REVIEW)
				.count();

		if (pendingReviewsCount > 0) {
			throw new DEIException(ErrorMessage.REVIEWS_STILL_PENDING, String.valueOf(pendingReviewsCount));
		}

		for (Exam exam : candidateExams) {
			if (exam.isReleased()) {
				exam.calculateObtainedScore();
				examRepository.save(exam);
			}
		}

		return candidateExams.stream()
				.map(GradeSummaryDto::new)
				.toList();
	}
}
