package pt.ulisboa.tecnico.rnl.dei.ems.statistics.dto;

import java.util.List;

public record StatisticsDto(
		long totalExams,
		long totalStudents,
		long countUploaded,
		long countSegmented,
		long countInDistribution,
		long countDistributed,
		long countCorrected,
		long countReleased,
		double correctionRate,
		double globalAverage,
		long totalReviews,
		long resolvedReviews,
		double reviewResolutionRate,
		List<DisciplineStatDto> disciplineStats) {

	public record DisciplineStatDto(
			long id,
			String name,
			String code,
			long totalExams,
			long countUploaded,
			long countSegmented,
			long countCorrected,
			double avgScore) {
	}
}
