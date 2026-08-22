package pt.ulisboa.tecnico.rnl.dei.ems.exam.dto;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question;

public record TeacherTaskDto(
		Long id,
		Long examId,
		String questionNumber,
		Double maxScore,
		Long disciplineId,
		String disciplineName,
		String disciplineCode,
		int pageNumber,
		Double score,
		String feedback,
		String status,
		boolean hasAnnotation
) {
	public TeacherTaskDto(Question q) {
		this(
				q.getId(),
				q.getExam() != null ? q.getExam().getId() : null,
				q.getQuestionNumber(),
				q.getMaxScore(),
				q.getDiscipline() != null ? q.getDiscipline().getId() : null,
				q.getDiscipline() != null ? q.getDiscipline().getName() : null,
				q.getDiscipline() != null ? q.getDiscipline().getCode() : null,
				q.getPageNumber(),
				q.getScore(),
				q.getFeedback(),
				q.getStatus() != null ? q.getStatus().name() : null,
				q.getAnnotatedImagePath() != null
		);
	}
}
