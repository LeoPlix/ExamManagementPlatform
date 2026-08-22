package pt.ulisboa.tecnico.rnl.dei.ems.exam.dto;

import java.util.Collections;
import java.util.List;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;

public record ExamDto(
		Long id,
		String title,
		Long schoolId,
		String schoolName,
		Long disciplineId,
		String disciplineName,
		Long studentId,
		String studentName,
		String studentEmail,
		String pdfFilename,
		int totalPages,
		String status,
		String createdAt,
		Double totalScore,
		Double obtainedScore,
		boolean gradesPublished,
		String gradesPublishedAt,
		boolean released,
		String releasedAt,
		String reviewDeadline,
		boolean viewRequested,
		int questionCount,
		List<QuestionDto> questions) {

	public ExamDto(Exam exam) {
		this(
				exam.getId(),
				exam.getTitle(),
				exam.getSchool() != null ? exam.getSchool().getId() : null,
				exam.getSchool() != null ? exam.getSchool().getName() : null,
				exam.getDiscipline() != null ? exam.getDiscipline().getId() : null,
				exam.getDiscipline() != null ? exam.getDiscipline().getName() : null,
				exam.getStudent() != null ? exam.getStudent().getId() : null,
				exam.getStudent() != null ? exam.getStudent().getName() : null,
				exam.getStudent() != null ? exam.getStudent().getEmail() : null,
				exam.getPdfFilename(),
				exam.getTotalPages(),
				exam.getStatus() != null ? exam.getStatus().name() : null,
				exam.getCreatedAt() != null ? exam.getCreatedAt().toString() : null,
				exam.getTotalScore(),
				(exam.isGradesPublished() || exam.isReleased() || exam.getStatus() == pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus.RELEASED) ? exam.getObtainedScore() : null,
				exam.isGradesPublished(),
				exam.getGradesPublishedAt() != null ? exam.getGradesPublishedAt().toString() : null,
				exam.isReleased(),
				exam.getReleasedAt() != null ? exam.getReleasedAt().toString() : null,
				exam.getReviewDeadline() != null ? exam.getReviewDeadline().toString() : null,
				exam.isViewRequested(),
				exam.getQuestions() != null ? exam.getQuestions().size() : 0,
				exam.getQuestions() != null
						? exam.getQuestions().stream().map(QuestionDto::new).toList()
						: Collections.emptyList()
		);
	}
}

