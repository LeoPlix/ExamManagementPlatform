package pt.ulisboa.tecnico.rnl.dei.ems.exam.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question.QuestionStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.SubmitEvaluationDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.TeacherTaskDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ExamRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.QuestionRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.ems.person.repository.PersonRepository;

@Service
@Transactional
public class EvaluationService {

	private final ExamRepository examRepository;
	private final QuestionRepository questionRepository;
	private final PersonRepository personRepository;
	private final FileStorageService fileStorageService;

	public EvaluationService(
			ExamRepository examRepository,
			QuestionRepository questionRepository,
			PersonRepository personRepository,
			FileStorageService fileStorageService) {
		this.examRepository = examRepository;
		this.questionRepository = questionRepository;
		this.personRepository = personRepository;
		this.fileStorageService = fileStorageService;
	}

	public Map<String, Object> distributeAllSegmentedExams() {
		// Cannot distribute while there are unsegmented exams
		List<Exam> unsegmentedExams = examRepository.findByStatus(ExamStatus.UPLOADED);
		if (!unsegmentedExams.isEmpty()) {
			throw new DEIException(ErrorMessage.CANNOT_DISTRIBUTE_UNSEGMENTED_EXAMS_EXIST, String.valueOf(unsegmentedExams.size()));
		}

		List<Exam> examsToDistribute = examRepository.findByStatus(ExamStatus.SEGMENTED);
		if (examsToDistribute.isEmpty()) {
			throw new DEIException(ErrorMessage.NO_QUESTIONS_TO_DISTRIBUTE);
		}

		// Group exams by discipline
		Map<Long, List<Exam>> examsByDiscipline = new HashMap<>();
		for (Exam exam : examsToDistribute) {
			examsByDiscipline.computeIfAbsent(exam.getDiscipline().getId(), k -> new ArrayList<>()).add(exam);
		}

		int totalDistributedQuestions = 0;

		for (Map.Entry<Long, List<Exam>> entry : examsByDiscipline.entrySet()) {
			Long discId = entry.getKey();
			List<Exam> disciplineExams = entry.getValue();

			List<Person> teachers = personRepository.findByTypeAndDisciplinesId(PersonType.TEACHER, discId);
			if (teachers.isEmpty()) {
				String discName = disciplineExams.get(0).getDiscipline().getName();
				throw new DEIException(ErrorMessage.NO_TEACHERS_FOR_DISCIPLINE, discName);
			}

			List<Question> questions = new ArrayList<>();
			for (Exam exam : disciplineExams) {
				questions.addAll(exam.getQuestions());
			}

			// Balance workload across teachers for this discipline
			Map<Long, Integer> teacherLoad = new HashMap<>();
			for (Person t : teachers) {
				teacherLoad.put(t.getId(), (int) questionRepository.countByEvaluatorIdAndScoreIsNull(t.getId()));
			}

			for (Question q : questions) {
				Person assignedTeacher = teachers.stream()
						.min(Comparator.comparingInt(t -> teacherLoad.getOrDefault(t.getId(), 0)))
						.orElse(teachers.get(0));

				q.setEvaluator(assignedTeacher);
				q.setStatus(QuestionStatus.PENDING_EVALUATION);
				questionRepository.save(q);

				teacherLoad.put(assignedTeacher.getId(), teacherLoad.getOrDefault(assignedTeacher.getId(), 0) + 1);
				totalDistributedQuestions++;
			}

			for (Exam exam : disciplineExams) {
				exam.setStatus(ExamStatus.DISTRIBUTED);
				examRepository.save(exam);
			}
		}

		Map<String, Object> result = new HashMap<>();
		result.put("distributedExamsCount", examsToDistribute.size());
		result.put("distributedQuestionsCount", totalDistributedQuestions);
		return result;
	}

	public Map<String, Object> distributeSegmentedExams(Long disciplineId, Long schoolId) {
		return distributeAllSegmentedExams();
	}

	@Transactional(readOnly = true)
	public List<TeacherTaskDto> getTeacherTasks(long teacherId, String statusFilter) {
		List<Question> questions;
		if ("PENDING".equalsIgnoreCase(statusFilter)) {
			questions = questionRepository.findByEvaluatorIdAndScoreIsNull(teacherId);
		} else if ("COMPLETED".equalsIgnoreCase(statusFilter)) {
			questions = questionRepository.findByEvaluatorIdAndScoreIsNotNull(teacherId);
		} else {
			questions = questionRepository.findByEvaluatorId(teacherId);
		}

		return questions.stream()
				.map(TeacherTaskDto::new)
				.toList();
	}

	public TeacherTaskDto submitTaskEvaluation(long teacherId, long questionId, SubmitEvaluationDto dto) {
		Question question = questionRepository.findById(questionId)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_QUESTION, Long.toString(questionId)));

		if (question.getEvaluator() == null || !question.getEvaluator().getId().equals(teacherId)) {
			throw new DEIException(ErrorMessage.TASK_NOT_ASSIGNED_TO_TEACHER);
		}

		if (dto.score() == null || dto.score() < 0 || dto.score() > question.getMaxScore()) {
			throw new DEIException(ErrorMessage.INVALID_SCORE_RANGE, String.valueOf(question.getMaxScore()));
		}

		question.setScore(dto.score());
		question.setFeedback(dto.feedback() != null ? dto.feedback().trim() : null);

		if (dto.annotatedImageData() != null && !dto.annotatedImageData().isBlank()) {
			String savedPath = fileStorageService.storeAnnotatedQuestionImage(dto.annotatedImageData());
			if (savedPath != null) {
				question.setAnnotatedImagePath(savedPath);
			}
		}

		question.setStatus(QuestionStatus.EVALUATED);
		questionRepository.save(question);

		Exam exam = question.getExam();
		if (exam != null) {
			exam.calculateObtainedScore();
			if (exam.isFullyEvaluated()) {
				exam.setStatus(ExamStatus.CORRECTED);
			}
			examRepository.save(exam);
		}

		return new TeacherTaskDto(question);
	}
}
