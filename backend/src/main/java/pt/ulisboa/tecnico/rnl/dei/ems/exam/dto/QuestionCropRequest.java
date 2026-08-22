package pt.ulisboa.tecnico.rnl.dei.ems.exam.dto;

public record QuestionCropRequest(
		String questionNumber,
		Double maxScore,
		int pageNumber,
		Double cropX,
		Double cropY,
		Double cropWidth,
		Double cropHeight,
		Long disciplineId) {
}
