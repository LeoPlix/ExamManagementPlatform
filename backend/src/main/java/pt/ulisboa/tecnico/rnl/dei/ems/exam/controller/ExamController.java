package pt.ulisboa.tecnico.rnl.dei.ems.exam.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.ExamDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.QuestionCropRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.QuestionDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.ExamService;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.ems.security.SecurityUtils;

@RestController
public class ExamController {

	private final ExamService examService;

	public ExamController(ExamService examService) {
		this.examService = examService;
	}

	@GetMapping("/exams/submissions-locked")
	public java.util.Map<String, Boolean> areSubmissionsLocked() {
		return java.util.Map.of("locked", examService.areSubmissionsLocked());
	}

	@PostMapping(value = "/exams", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('EXAM_UPLOAD')")
	public ExamDto createExam(
			@RequestParam("file") MultipartFile file,
			@RequestParam("title") String title,
			@RequestParam("schoolId") Long schoolId,
			@RequestParam("disciplineId") Long disciplineId,
			@RequestParam("studentId") Long studentId) {
		return examService.createExam(file, title, schoolId, disciplineId, studentId);
	}

	@GetMapping("/exams")
	@PreAuthorize("hasAuthority('EXAM_READ')")
	public List<ExamDto> getExams(
			@RequestParam(value = "schoolId", required = false) Long schoolId,
			@RequestParam(value = "disciplineId", required = false) Long disciplineId,
			@RequestParam(value = "studentId", required = false) Long studentId) {
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.STUDENT) {
			return examService.getExams(null, null, current.getId());
		}
		return examService.getExams(schoolId, disciplineId, studentId);
	}

	@GetMapping("/exams/{id}")
	@PreAuthorize("hasAuthority('EXAM_READ')")
	public ExamDto getExam(@PathVariable long id) {
		Person current = SecurityUtils.currentPerson();
		ExamDto exam = examService.getExam(id);
		if (current != null && current.getType() == PersonType.STUDENT) {
			if (exam.studentId() == null || !exam.studentId().equals(current.getId())) {
				throw new DEIException(ErrorMessage.ACCESS_DENIED);
			}
		}
		return exam;
	}


	@GetMapping("/exams/{id}/pdf")
	@PreAuthorize("hasAuthority('EXAM_READ')")
	public ResponseEntity<Resource> getExamPdf(@PathVariable long id) {
		Resource resource = examService.getExamPdfResource(id);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@GetMapping(value = "/exams/{id}/pages/{pageNumber}/image", produces = MediaType.IMAGE_PNG_VALUE)
	@PreAuthorize("hasAuthority('EXAM_READ')")
	public ResponseEntity<byte[]> getExamPageImage(
			@PathVariable long id,
			@PathVariable int pageNumber) {
		byte[] pngData = examService.renderExamPagePng(id, pageNumber);
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_PNG)
				.body(pngData);
	}

	@PostMapping("/exams/{id}/questions")
	@PreAuthorize("hasAuthority('EXAM_SEGMENT')")
	public QuestionDto addQuestion(
			@PathVariable long id,
			@RequestBody QuestionCropRequest req) {
		return examService.addQuestion(id, req);
	}

	@GetMapping("/exams/{id}/questions")
	@PreAuthorize("hasAuthority('EXAM_READ')")
	public List<QuestionDto> getExamQuestions(@PathVariable long id) {
		return examService.getExamQuestions(id);
	}

	@DeleteMapping("/exams/{id}/questions/{questionId}")
	@PreAuthorize("hasAuthority('EXAM_SEGMENT')")
	public void deleteQuestion(
			@PathVariable long id,
			@PathVariable long questionId) {
		examService.deleteQuestion(id, questionId);
	}

	@PostMapping("/exams/{id}/complete-segmentation")
	@PreAuthorize("hasAuthority('EXAM_SEGMENT')")
	public ExamDto completeSegmentation(@PathVariable long id) {
		return examService.completeSegmentation(id);
	}

	@GetMapping(value = "/questions/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
	@PreAuthorize("hasAnyAuthority('EXAM_READ', 'EVALUATION_READ', 'REVIEW_EVALUATE', 'REVIEW_REQUEST')")
	public ResponseEntity<Resource> getQuestionImage(@PathVariable long id) {
		Resource resource = examService.getQuestionImageResource(id);
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_PNG)
				.body(resource);
	}

	@GetMapping(value = "/questions/{id}/annotated-image", produces = MediaType.IMAGE_PNG_VALUE)
	@PreAuthorize("hasAnyAuthority('EXAM_READ', 'EVALUATION_READ', 'REVIEW_EVALUATE', 'REVIEW_REQUEST')")
	public ResponseEntity<Resource> getQuestionAnnotatedImage(@PathVariable long id) {
		Resource resource = examService.getQuestionAnnotatedImageResource(id);
		return ResponseEntity.ok()
				.contentType(MediaType.IMAGE_PNG)
				.body(resource);
	}

	@DeleteMapping("/exams/{id}")
	@PreAuthorize("hasAuthority('EXAM_DELETE')")
	public void deleteExam(@PathVariable long id) {
		examService.deleteExam(id);
	}
}
