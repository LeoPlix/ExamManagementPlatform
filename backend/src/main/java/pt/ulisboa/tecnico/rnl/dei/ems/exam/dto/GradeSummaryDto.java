package pt.ulisboa.tecnico.rnl.dei.ems.exam.dto;

import java.time.LocalDateTime;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question.QuestionStatus;

public record GradeSummaryDto(
		Long examId,
		String examTitle,
		Long disciplineId,
		String disciplineName,
		String disciplineCode,
		Long schoolId,
		String schoolName,
		Long studentId,
		String studentName,
		String studentEmail,
		Double totalScore,
		Double obtainedScore,
		boolean gradesPublished,
		LocalDateTime gradesPublishedAt,
		String status,
		boolean released,
		LocalDateTime releasedAt,
		LocalDateTime reviewDeadline,
		boolean viewRequested,
		int questionCount,
		int pendingReviewCount,
		int reviewedCount
) {
	public GradeSummaryDto(Exam exam) {
		this(
				exam.getId(),
				exam.getTitle(),
				exam.getDiscipline() != null ? exam.getDiscipline().getId() : null,
				exam.getDiscipline() != null ? exam.getDiscipline().getName() : null,
				exam.getDiscipline() != null ? exam.getDiscipline().getCode() : null,
				exam.getSchool() != null ? exam.getSchool().getId() : null,
				exam.getSchool() != null ? exam.getSchool().getName() : null,
				exam.getStudent() != null ? exam.getStudent().getId() : null,
				exam.getStudent() != null ? exam.getStudent().getName() : null,
				exam.getStudent() != null ? exam.getStudent().getEmail() : null,
				exam.getTotalScore(),
				(exam.isGradesPublished() || exam.isReleased() || exam.getStatus() == Exam.ExamStatus.RELEASED) ? exam.getObtainedScore() : null,
				exam.isGradesPublished(),
				exam.getGradesPublishedAt(),
				exam.getStatus() != null ? exam.getStatus().name() : null,
				exam.isReleased(),
				exam.getReleasedAt(),
				exam.getReviewDeadline(),
				exam.isViewRequested(),
				exam.getQuestions() != null ? exam.getQuestions().size() : 0,
				exam.getQuestions() != null ? (int) exam.getQuestions().stream().filter(q -> q.getStatus() == QuestionStatus.IN_REVIEW).count() : 0,
				exam.getQuestions() != null ? (int) exam.getQuestions().stream().filter(q -> q.getStatus() == QuestionStatus.REVIEWED).count() : 0
		);
	}
}
