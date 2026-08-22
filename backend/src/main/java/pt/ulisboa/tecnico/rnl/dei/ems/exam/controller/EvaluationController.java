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
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.SubmitEvaluationDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.TeacherTaskDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.EvaluationService;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.security.SecurityUtils;

@RestController
public class EvaluationController {

	private final EvaluationService evaluationService;

	public EvaluationController(EvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}

	@PostMapping("/exams/distribute")
	@PreAuthorize("hasAuthority('EXAM_DISTRIBUTE')")
	public Map<String, Object> distributeExams() {
		return evaluationService.distributeAllSegmentedExams();
	}

	@GetMapping("/teacher/tasks")
	@PreAuthorize("hasAuthority('EVALUATION_READ')")
	public List<TeacherTaskDto> getTeacherTasks(@RequestParam(value = "status", required = false) String status) {
		Person teacher = SecurityUtils.currentPerson();
		if (teacher == null) {
			throw new DEIException(ErrorMessage.NOT_AUTHENTICATED);
		}
		return evaluationService.getTeacherTasks(teacher.getId(), status);
	}

	@PostMapping("/teacher/tasks/{questionId}/submit")
	@PreAuthorize("hasAuthority('EVALUATION_SUBMIT')")
	public TeacherTaskDto submitTaskEvaluation(
			@PathVariable long questionId,
			@RequestBody SubmitEvaluationDto dto) {
		Person teacher = SecurityUtils.currentPerson();
		if (teacher == null) {
			throw new DEIException(ErrorMessage.NOT_AUTHENTICATED);
		}
		return evaluationService.submitTaskEvaluation(teacher.getId(), questionId, dto);
	}
}
