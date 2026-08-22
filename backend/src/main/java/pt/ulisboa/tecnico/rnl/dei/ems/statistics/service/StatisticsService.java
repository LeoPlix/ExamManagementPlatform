package pt.ulisboa.tecnico.rnl.dei.ems.statistics.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.ReviewRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.ReviewRequest.ReviewStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ExamRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ReviewRequestRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.DisciplineRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.statistics.dto.StatisticsDto;
import pt.ulisboa.tecnico.rnl.dei.ems.statistics.dto.StatisticsDto.DisciplineStatDto;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

	private final ExamRepository examRepository;
	private final ReviewRequestRepository reviewRequestRepository;
	private final DisciplineRepository disciplineRepository;

	public StatisticsService(
			ExamRepository examRepository,
			ReviewRequestRepository reviewRequestRepository,
			DisciplineRepository disciplineRepository) {
		this.examRepository = examRepository;
		this.reviewRequestRepository = reviewRequestRepository;
		this.disciplineRepository = disciplineRepository;
	}

	public StatisticsDto getStatistics() {
		List<Exam> exams = examRepository.findAll();
		List<ReviewRequest> reviews = reviewRequestRepository.findAll();
		List<Discipline> disciplines = disciplineRepository.findAll();

		long totalExams = exams.size();

		Set<Long> studentIds = exams.stream()
				.map(Exam::getStudent)
				.filter(Objects::nonNull)
				.map(pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person::getId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		long totalStudents = studentIds.size();

		long countUploaded = exams.stream().filter(e -> e.getStatus() == ExamStatus.UPLOADED).count();
		long countSegmented = exams.stream().filter(e -> e.getStatus() == ExamStatus.SEGMENTED).count();
		long countInDistribution = exams.stream().filter(e -> e.getStatus() == ExamStatus.IN_DISTRIBUTION).count();
		long countDistributed = exams.stream().filter(e -> e.getStatus() == ExamStatus.DISTRIBUTED).count();
		long countCorrected = exams.stream().filter(e -> e.getStatus() == ExamStatus.CORRECTED && !e.isReleased()).count();
		long countReleased = exams.stream().filter(e -> e.getStatus() == ExamStatus.RELEASED || e.isReleased()).count();

		List<Exam> evaluatedExams = exams.stream()
				.filter(e -> (e.getStatus() == ExamStatus.CORRECTED || e.getStatus() == ExamStatus.RELEASED)
						&& e.getObtainedScore() != null)
				.toList();

		double correctionRate = totalExams > 0 ? ((double) evaluatedExams.size() / totalExams) * 100.0 : 0.0;

		double globalAverage = evaluatedExams.isEmpty() ? 0.0
				: evaluatedExams.stream().mapToDouble(Exam::getObtainedScore).average().orElse(0.0);

		long totalReviews = reviews.size();
		long resolvedReviews = reviews.stream().filter(r -> r.getStatus() == ReviewStatus.RESOLVED).count();
		double reviewResolutionRate = totalReviews > 0 ? ((double) resolvedReviews / totalReviews) * 100.0 : 0.0;

		List<DisciplineStatDto> disciplineStats = disciplines.stream().map(d -> {
			List<Exam> discExams = exams.stream()
					.filter(e -> e.getDiscipline() != null && Objects.equals(e.getDiscipline().getId(), d.getId()))
					.toList();
			long discUploaded = discExams.stream()
					.filter(e -> e.getStatus() == ExamStatus.UPLOADED)
					.count();
			long discSegmented = discExams.stream()
					.filter(e -> e.getStatus() == ExamStatus.SEGMENTED)
					.count();
			List<Exam> discEvaluated = discExams.stream()
					.filter(e -> (e.getStatus() == ExamStatus.CORRECTED || e.getStatus() == ExamStatus.RELEASED)
							&& e.getObtainedScore() != null)
					.toList();
			double avg = discEvaluated.isEmpty() ? 0.0
					: discEvaluated.stream().mapToDouble(Exam::getObtainedScore).average().orElse(0.0);

			return new DisciplineStatDto(
					d.getId(),
					d.getName(),
					d.getCode(),
					discExams.size(),
					discUploaded,
					discSegmented,
					discEvaluated.size(),
					avg);
		}).toList();

		return new StatisticsDto(
				totalExams,
				totalStudents,
				countUploaded,
				countSegmented,
				countInDistribution,
				countDistributed,
				countCorrected,
				countReleased,
				correctionRate,
				globalAverage,
				totalReviews,
				resolvedReviews,
				reviewResolutionRate,
				disciplineStats);
	}
}
