package pt.ulisboa.tecnico.rnl.dei.ems.exam.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.CreateReviewDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.ReviewRequestDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.SubmitReviewDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.ReviewService;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.security.SecurityUtils;

@RestController
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping("/student/exams/{examId}/reviews")
	@PreAuthorize("hasAuthority('REVIEW_REQUEST')")
	public ReviewRequestDto createReviewRequest(
			@PathVariable long examId,
			@RequestBody CreateReviewDto dto) {
		Person student = SecurityUtils.currentPerson();
		if (student == null) {
			throw new DEIException(ErrorMessage.NOT_AUTHENTICATED);
		}
		return reviewService.createReviewRequest(student.getId(), examId, dto);
	}

	@GetMapping("/student/reviews")
	@PreAuthorize("hasAuthority('REVIEW_REQUEST')")
	public List<ReviewRequestDto> getStudentReviews() {
		Person student = SecurityUtils.currentPerson();
		if (student == null) {
			throw new DEIException(ErrorMessage.NOT_AUTHENTICATED);
		}
		return reviewService.getStudentReviews(student.getId());
	}

	@PostMapping("/reviews/distribute")
	@PreAuthorize("hasAuthority('REVIEW_DISTRIBUTE')")
	public Map<String, Object> distributeReviews(@RequestParam(value = "disciplineId", required = false) Long disciplineId) {
		return reviewService.closeTimelineAndDistributeReviews(disciplineId);
	}

	@GetMapping("/teacher/reviews")
	@PreAuthorize("hasAuthority('REVIEW_EVALUATE')")
	public List<ReviewRequestDto> getTeacherReviewTasks() {
		Person teacher = SecurityUtils.currentPerson();
		if (teacher == null) {
			throw new DEIException(ErrorMessage.NOT_AUTHENTICATED);
		}
		return reviewService.getTeacherReviewTasks(teacher.getId());
	}

	@PostMapping("/teacher/reviews/{reviewId}/submit")
	@PreAuthorize("hasAuthority('REVIEW_EVALUATE')")
	public ReviewRequestDto submitReviewEvaluation(
			@PathVariable long reviewId,
			@RequestBody SubmitReviewDto dto) {
		Person teacher = SecurityUtils.currentPerson();
		if (teacher == null) {
			throw new DEIException(ErrorMessage.NOT_AUTHENTICATED);
		}
		return reviewService.submitReviewEvaluation(teacher.getId(), reviewId, dto);
	}
}
