package pt.ulisboa.tecnico.rnl.dei.ems.exam.dto;

public record SubmitEvaluationDto(
		Double score,
		String feedback,
		String annotatedImageData
) {
	public SubmitEvaluationDto(Double score, String feedback) {
		this(score, feedback, null);
	}
}
