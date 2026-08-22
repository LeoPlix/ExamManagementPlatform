package pt.ulisboa.tecnico.rnl.dei.ems.exam.service;

import java.util.List;

import java.util.Objects;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.ExamDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.QuestionCropRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.QuestionDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ExamRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.QuestionRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.ems.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.School;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.DisciplineRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.SchoolRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.security.SecurityUtils;

@Service
@Transactional
public class ExamService {

	private final ExamRepository examRepository;
	private final QuestionRepository questionRepository;
	private final SchoolRepository schoolRepository;
	private final DisciplineRepository disciplineRepository;
	private final PersonRepository personRepository;
	private final FileStorageService fileStorageService;
	private final PdfProcessingService pdfProcessingService;

	public ExamService(
			ExamRepository examRepository,
			QuestionRepository questionRepository,
			SchoolRepository schoolRepository,
			DisciplineRepository disciplineRepository,
			PersonRepository personRepository,
			FileStorageService fileStorageService,
			PdfProcessingService pdfProcessingService) {
		this.examRepository = examRepository;
		this.questionRepository = questionRepository;
		this.schoolRepository = schoolRepository;
		this.disciplineRepository = disciplineRepository;
		this.personRepository = personRepository;
		this.fileStorageService = fileStorageService;
		this.pdfProcessingService = pdfProcessingService;
	}

	public Exam fetchExamOrThrow(long id) {
		return examRepository.findById(id)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_EXAM, Long.toString(id)));
	}

	public Question fetchQuestionOrThrow(long id) {
		return questionRepository.findById(id)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(id)));
	}

	private static final List<ExamStatus> DISTRIBUTED_STATUSES = List.of(
			ExamStatus.DISTRIBUTED,
			ExamStatus.CORRECTED,
			ExamStatus.RELEASED
	);

	@Transactional(readOnly = true)
	public boolean areSubmissionsLocked() {
		return examRepository.existsByStatusIn(DISTRIBUTED_STATUSES);
	}

	@Transactional(readOnly = true)
	public boolean isDisciplineLocked(Long disciplineId) {
		if (disciplineId == null) return false;
		return examRepository.existsByDisciplineIdAndStatusIn(disciplineId, DISTRIBUTED_STATUSES);
	}

	private void checkNotDistributedLock(Long disciplineId, Long schoolId) {
		if (disciplineId != null && isDisciplineLocked(disciplineId)) {
			throw new DEIException(ErrorMessage.EXAMS_LOCKED_AFTER_DISTRIBUTION);
		}
	}

	public ExamDto createExam(MultipartFile file, String title, Long schoolId, Long disciplineId, Long studentId) {
		if (title == null || title.trim().isBlank()) {
			throw new DEIException(ErrorMessage.EXAM_TITLE_NOT_VALID);
		}

		Long targetSchoolId = schoolId;
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.SCHOOL_STAFF) {
			if (current.getSchool() == null) {
				throw new DEIException(ErrorMessage.ACCESS_DENIED);
			}
			targetSchoolId = current.getSchool().getId();
		}

		if (targetSchoolId == null) {
			throw new DEIException(ErrorMessage.NO_SUCH_SCHOOL, "null");
		}
		if (disciplineId == null) {
			throw new DEIException(ErrorMessage.NO_SUCH_DISCIPLINE, "null");
		}
		if (studentId == null) {
			throw new DEIException(ErrorMessage.NO_SUCH_PERSON, "null");
		}

		final Long finalSchoolId = targetSchoolId;
		checkNotDistributedLock(disciplineId, finalSchoolId);

		School school = schoolRepository.findById(finalSchoolId)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_SCHOOL, Long.toString(finalSchoolId)));
		Discipline discipline = disciplineRepository.findById(disciplineId)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_DISCIPLINE, Long.toString(disciplineId)));
		Person student = personRepository.findById(studentId)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, Long.toString(studentId)));

		if (student.getType() != PersonType.STUDENT) {
			throw new DEIException(ErrorMessage.EXAM_STUDENT_NOT_VALID);
		}
		if (current != null && current.getType() == PersonType.SCHOOL_STAFF) {
			if (student.getSchool() != null && !Objects.equals(student.getSchool().getId(), school.getId())) {
				throw new DEIException(ErrorMessage.EXAM_STUDENT_NOT_VALID);
			}
		}

		if (examRepository.existsByStudentIdAndDisciplineId(studentId, disciplineId)) {
			throw new DEIException(ErrorMessage.STUDENT_ALREADY_HAS_EXAM_FOR_DISCIPLINE, discipline.getName());
		}

		String pdfPath = fileStorageService.storeExamPdf(file);
		int totalPages = pdfProcessingService.getPageCount(pdfPath);
		String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "exam.pdf";

		Exam exam = new Exam(title.trim(), school, discipline, student, pdfPath, originalFilename, totalPages);
		return new ExamDto(examRepository.save(exam));
	}

	@Transactional(readOnly = true)
	public List<ExamDto> getExams(Long schoolId, Long disciplineId, Long studentId) {
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getType() == PersonType.SCHOOL_STAFF) {
			if (current.getSchool() != null) {
				schoolId = current.getSchool().getId();
			}
		}

		List<Exam> exams;
		if (schoolId != null && disciplineId != null) {
			exams = examRepository.findBySchoolIdAndDisciplineId(schoolId, disciplineId);
		} else if (schoolId != null) {
			exams = examRepository.findBySchoolId(schoolId);
		} else if (disciplineId != null) {
			exams = examRepository.findByDisciplineId(disciplineId);
		} else if (studentId != null) {
			exams = examRepository.findByStudentId(studentId);
		} else {
			exams = examRepository.findAll();
		}

		return exams.stream().map(ExamDto::new).toList();
	}

	@Transactional(readOnly = true)
	public ExamDto getExam(long id) {
		return new ExamDto(fetchExamOrThrow(id));
	}

	@Transactional(readOnly = true)
	public Resource getExamPdfResource(long id) {
		Exam exam = fetchExamOrThrow(id);
		return fileStorageService.loadFileAsResource(exam.getPdfPath());
	}

	@Transactional(readOnly = true)
	public byte[] renderExamPagePng(long id, int pageNumber) {
		Exam exam = fetchExamOrThrow(id);
		return pdfProcessingService.renderPageToPng(exam.getPdfPath(), pageNumber);
	}

	public QuestionDto addQuestion(long examId, QuestionCropRequest req) {
		Exam exam = fetchExamOrThrow(examId);

		if (exam.getStatus() != ExamStatus.UPLOADED && exam.getStatus() != ExamStatus.SEGMENTED) {
			throw new DEIException(ErrorMessage.EXAM_ALREADY_DISTRIBUTED);
		}
		checkNotDistributedLock(exam.getDiscipline().getId(), exam.getSchool().getId());

		if (req.questionNumber() == null || req.questionNumber().trim().isBlank()) {
			throw new DEIException(ErrorMessage.QUESTION_NUMBER_NOT_VALID);
		}
		if (req.maxScore() == null || req.maxScore() <= 0) {
			throw new DEIException(ErrorMessage.QUESTION_MAX_SCORE_NOT_VALID);
		}

		double currentTotal = exam.getQuestions().stream().mapToDouble(Question::getMaxScore).sum();
		if (currentTotal + req.maxScore() > 20.001) {
			throw new DEIException(ErrorMessage.EXAM_SCORE_EXCEEDS_20,
					String.format(java.util.Locale.US, "%.1f", currentTotal),
					String.format(java.util.Locale.US, "%.1f", req.maxScore()));
		}

		if (req.pageNumber() < 1 || req.pageNumber() > exam.getTotalPages()) {
			throw new DEIException(ErrorMessage.QUESTION_PAGE_INVALID);
		}

		Discipline discipline = exam.getDiscipline();
		if (req.disciplineId() != null) {
			discipline = disciplineRepository.findById(req.disciplineId())
					.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_DISCIPLINE, Long.toString(req.disciplineId())));
		}

		byte[] croppedPng = pdfProcessingService.cropPageRegionToPng(
				exam.getPdfPath(),
				req.pageNumber(),
				req.cropX(),
				req.cropY(),
				req.cropWidth(),
				req.cropHeight()
		);

		String imagePath = fileStorageService.storeQuestionImage(croppedPng, "png");
		int nextIndex = exam.getQuestions().size();

		Question question = new Question(
				exam,
				discipline,
				req.questionNumber().trim(),
				req.maxScore(),
				imagePath,
				req.pageNumber(),
				req.cropX(),
				req.cropY(),
				req.cropWidth(),
				req.cropHeight(),
				nextIndex
		);

		Question savedQuestion = questionRepository.save(question);
		exam.getQuestions().add(savedQuestion);
		exam.calculateTotalScore();
		if (Math.abs(exam.getTotalScore() - 20.0) > 0.001) {
			exam.setStatus(ExamStatus.UPLOADED);
		}
		examRepository.save(exam);

		return new QuestionDto(savedQuestion);
	}

	@Transactional(readOnly = true)
	public List<QuestionDto> getExamQuestions(long examId) {
		fetchExamOrThrow(examId);
		return questionRepository.findByExamIdOrderByOrderIndexAsc(examId).stream()
				.map(QuestionDto::new)
				.toList();
	}

	public void deleteQuestion(long examId, long questionId) {
		Exam exam = fetchExamOrThrow(examId);
		Question question = fetchQuestionOrThrow(questionId);

		if (exam.getStatus() != ExamStatus.UPLOADED && exam.getStatus() != ExamStatus.SEGMENTED) {
			throw new DEIException(ErrorMessage.EXAM_ALREADY_DISTRIBUTED);
		}
		checkNotDistributedLock(exam.getDiscipline().getId(), exam.getSchool().getId());

		if (!question.getExam().getId().equals(exam.getId())) {
			throw new DEIException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(questionId));
		}

		fileStorageService.deleteFileIfExists(question.getImagePath());
		exam.getQuestions().remove(question);
		questionRepository.delete(question);

		exam.calculateTotalScore();
		exam.setStatus(ExamStatus.UPLOADED);
		examRepository.save(exam);
	}

	public ExamDto completeSegmentation(long examId) {
		Exam exam = fetchExamOrThrow(examId);

		if (exam.getStatus() != ExamStatus.UPLOADED && exam.getStatus() != ExamStatus.SEGMENTED) {
			throw new DEIException(ErrorMessage.EXAM_ALREADY_DISTRIBUTED);
		}
		checkNotDistributedLock(exam.getDiscipline().getId(), exam.getSchool().getId());

		if (exam.getQuestions() == null || exam.getQuestions().isEmpty()) {
			throw new DEIException(ErrorMessage.EXAM_NO_QUESTIONS);
		}

		double totalScore = exam.getQuestions().stream().mapToDouble(Question::getMaxScore).sum();
		if (Math.abs(totalScore - 20.0) > 0.001) {
			throw new DEIException(ErrorMessage.EXAM_TOTAL_SCORE_NOT_20,
					String.format(java.util.Locale.US, "%.1f", totalScore));
		}

		exam.setStatus(ExamStatus.SEGMENTED);
		exam.calculateTotalScore();
		return new ExamDto(examRepository.save(exam));
	}

	@Transactional(readOnly = true)
	public Resource getQuestionImageResource(long questionId) {
		Question question = fetchQuestionOrThrow(questionId);
		return fileStorageService.loadFileAsResource(question.getImagePath());
	}

	@Transactional(readOnly = true)
	public Resource getQuestionAnnotatedImageResource(long questionId) {
		Question question = fetchQuestionOrThrow(questionId);
		if (question.getAnnotatedImagePath() != null && !question.getAnnotatedImagePath().isBlank()) {
			try {
				return fileStorageService.loadFileAsResource(question.getAnnotatedImagePath());
			} catch (Exception ignored) {
				// Fallback to original image
			}
		}
		return fileStorageService.loadFileAsResource(question.getImagePath());
	}

	public void deleteExam(long id) {
		Exam exam = fetchExamOrThrow(id);

		if (exam.getStatus() != ExamStatus.UPLOADED && exam.getStatus() != ExamStatus.SEGMENTED) {
			throw new DEIException(ErrorMessage.EXAM_CANNOT_DELETE);
		}
		checkNotDistributedLock(exam.getDiscipline().getId(), exam.getSchool().getId());

		if (exam.getQuestions() != null) {
			for (Question q : exam.getQuestions()) {
				fileStorageService.deleteFileIfExists(q.getImagePath());
				fileStorageService.deleteFileIfExists(q.getAnnotatedImagePath());
			}
		}

		fileStorageService.deleteFileIfExists(exam.getPdfPath());
		examRepository.delete(exam);
	}
}
