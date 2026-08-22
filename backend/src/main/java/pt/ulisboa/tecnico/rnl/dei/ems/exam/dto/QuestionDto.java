package pt.ulisboa.tecnico.rnl.dei.ems.exam.dto;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question;

public record QuestionDto(
		Long id,
		Long examId,
		Long disciplineId,
		String disciplineName,
		String questionNumber,
		Double maxScore,
		String imagePath,
		int pageNumber,
		Double cropX,
		Double cropY,
		Double cropWidth,
		Double cropHeight,
		int orderIndex,
		Double score,
		String feedback,
		String status,
		boolean hasAnnotation) {

	public QuestionDto(Question question) {
		this(
				question.getId(),
				question.getExam() != null ? question.getExam().getId() : null,
				question.getDiscipline() != null ? question.getDiscipline().getId() : null,
				question.getDiscipline() != null ? question.getDiscipline().getName() : null,
				question.getQuestionNumber(),
				question.getMaxScore(),
				question.getImagePath(),
				question.getPageNumber(),
				question.getCropX(),
				question.getCropY(),
				question.getCropWidth(),
				question.getCropHeight(),
				question.getOrderIndex(),
				question.getScore(),
				question.getFeedback(),
				question.getStatus() != null ? question.getStatus().name() : null,
				question.getAnnotatedImagePath() != null
		);
	}
}

