package pt.ulisboa.tecnico.rnl.dei.ems.exam.dto;

import java.time.LocalDateTime;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.ReviewRequest;

public record ReviewRequestDto(
		Long id,
		Long questionId,
		Long examId,
		String examTitle,
		String questionNumber,
		Double maxScore,
		String disciplineName,
		Long studentId,
		String studentName,
		String justification,
		Double originalScore,
		Double revisedScore,
		Long reviewerId,
		String reviewerName,
		String reviewerFeedback,
		String status,
		LocalDateTime createdAt,
		LocalDateTime resolvedAt
) {
	public ReviewRequestDto(ReviewRequest r) {
		this(
				r.getId(),
				r.getQuestion() != null ? r.getQuestion().getId() : null,
				r.getQuestion() != null && r.getQuestion().getExam() != null ? r.getQuestion().getExam().getId() : null,
				r.getQuestion() != null && r.getQuestion().getExam() != null ? r.getQuestion().getExam().getTitle() : null,
				r.getQuestion() != null ? r.getQuestion().getQuestionNumber() : null,
				r.getQuestion() != null ? r.getQuestion().getMaxScore() : null,
				r.getQuestion() != null && r.getQuestion().getDiscipline() != null ? r.getQuestion().getDiscipline().getName() : null,
				r.getStudent() != null ? r.getStudent().getId() : null,
				r.getStudent() != null ? r.getStudent().getName() : null,
				r.getJustification(),
				r.getOriginalScore(),
				r.getRevisedScore(),
				r.getReviewer() != null ? r.getReviewer().getId() : null,
				r.getReviewer() != null ? r.getReviewer().getName() : null,
				r.getReviewerFeedback(),
				r.getStatus() != null ? r.getStatus().name() : null,
				r.getCreatedAt(),
				r.getResolvedAt()
		);
	}
}
