package pt.ulisboa.tecnico.rnl.dei.ems.exam.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.ExamDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.GradeSummaryDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.GradeService;

@RestController
public class GradeController {

	private final GradeService gradeService;

	public GradeController(GradeService gradeService) {
		this.gradeService = gradeService;
	}

	@GetMapping("/grades")
	@PreAuthorize("hasAuthority('GRADES_READ')")
	public List<GradeSummaryDto> getGrades(
			@RequestParam(value = "schoolId", required = false) Long schoolId,
			@RequestParam(value = "disciplineId", required = false) Long disciplineId) {
		return gradeService.getGrades(schoolId, disciplineId);
	}

	@PostMapping("/grades/publish-initial")
	@PreAuthorize("hasAuthority('EXAM_RELEASE')")
	public List<GradeSummaryDto> publishInitialGrades(
			@RequestParam(value = "schoolId", required = false) Long schoolId,
			@RequestParam(value = "disciplineId", required = false) Long disciplineId) {
		return gradeService.publishInitialGrades(schoolId, disciplineId);
	}

	@PostMapping("/exams/{id}/request-view")
	@PreAuthorize("hasAuthority('REVIEW_REQUEST')")
	public ExamDto requestExamView(@PathVariable long id) {
		return gradeService.requestExamView(id);
	}

	@PostMapping("/exams/{id}/release")
	@PreAuthorize("hasAuthority('EXAM_RELEASE')")
	public GradeSummaryDto releaseExam(@PathVariable long id) {
		return gradeService.releaseExam(id);
	}

	@PostMapping("/exams/bulk-release")
	@PreAuthorize("hasAuthority('EXAM_RELEASE')")
	public List<GradeSummaryDto> bulkRelease(
			@RequestParam(value = "schoolId", required = false) Long schoolId,
			@RequestParam(value = "disciplineId", required = false) Long disciplineId) {
		return gradeService.bulkRelease(schoolId, disciplineId);
	}

	@PostMapping("/exams/publish-reviews")
	@PreAuthorize("hasAuthority('EXAM_RELEASE')")
	public List<GradeSummaryDto> publishReviewGrades(
			@RequestParam(value = "schoolId", required = false) Long schoolId,
			@RequestParam(value = "disciplineId", required = false) Long disciplineId) {
		return gradeService.publishReviewGrades(schoolId, disciplineId);
	}
}
