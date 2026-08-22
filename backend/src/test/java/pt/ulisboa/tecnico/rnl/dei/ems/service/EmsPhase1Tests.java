package pt.ulisboa.tecnico.rnl.dei.ems.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.ExamDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.QuestionCropRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.dto.QuestionDto;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.ExamService;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.ems.person.service.PersonService;
import pt.ulisboa.tecnico.rnl.dei.ems.school.dto.DisciplineDto;
import pt.ulisboa.tecnico.rnl.dei.ems.school.dto.SchoolDto;
import pt.ulisboa.tecnico.rnl.dei.ems.school.service.DisciplineService;
import pt.ulisboa.tecnico.rnl.dei.ems.school.service.SchoolService;

@SpringBootTest
@Transactional
public class EmsPhase1Tests {

	@Autowired
	private SchoolService schoolService;

	@Autowired
	private DisciplineService disciplineService;

	@Autowired
	private PersonService personService;

	@Autowired
	private ExamService examService;

	private SchoolDto school;
	private DisciplineDto discipline;
	private PersonDto student;

	@BeforeEach
	void setUp() {
		school = schoolService.createSchool(new SchoolDto(null, "Escola Teste", "ESC-TEST-" + System.nanoTime(), "Lisboa"));
		discipline = disciplineService.createDiscipline(new DisciplineDto(null, "Geografia A", "GEO-" + System.nanoTime()));
		student = personService.createPerson(new PersonDto(
				null,
				"Aluno Teste",
				"alunoteste" + System.nanoTime() + "@test.pt",
				"pass123",
				"STUDENT",
				school.id(),
				null,
				null,
				null
		));
	}

	@Test
	void testSchoolCrud() {
		List<SchoolDto> schools = schoolService.getSchools();
		assertFalse(schools.isEmpty());

		SchoolDto found = schoolService.getSchool(school.id());
		assertEquals(school.name(), found.name());

		SchoolDto updated = schoolService.updateSchool(school.id(), new SchoolDto(school.id(), "Escola Atualizada", school.code(), "Norte"));
		assertEquals("Escola Atualizada", updated.name());
		assertEquals("Norte", updated.region());
	}

	@Test
	void testDisciplineCrud() {
		List<DisciplineDto> disciplines = disciplineService.getDisciplines();
		assertFalse(disciplines.isEmpty());

		DisciplineDto found = disciplineService.getDiscipline(discipline.id());
		assertEquals(discipline.name(), found.name());

		DisciplineDto updated = disciplineService.updateDiscipline(discipline.id(), new DisciplineDto(discipline.id(), "Geografia B", discipline.code()));
		assertEquals("Geografia B", updated.name());
	}

	@Test
	void testPersonWithSchoolAndDisciplines() {
		PersonDto teacher = personService.createPerson(new PersonDto(
				null,
				"Professor Teste",
				"profteste" + System.nanoTime() + "@test.pt",
				"pass123",
				"TEACHER",
				school.id(),
				null,
				Set.of(discipline.id()),
				null
		));

		assertNotNull(teacher.id());
		assertEquals(school.id(), teacher.schoolId());
		assertTrue(teacher.disciplineIds().contains(discipline.id()));
	}

	@Test
	void testExamUploadAndQuestionSegmentation() throws Exception {
		// Create mock PDF
		byte[] mockPdfBytes = generateSimplePdfBytes();
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file",
				"teste-exame.pdf",
				"application/pdf",
				mockPdfBytes
		);

		ExamDto exam = examService.createExam(multipartFile, "Exame Geografia 2026", school.id(), discipline.id(), student.id());
		assertNotNull(exam.id());
		assertEquals("UPLOADED", exam.status());
		assertEquals(1, exam.totalPages());
		assertEquals(0, exam.questionCount());

		// Add Question 1 (Full page or cropped)
		QuestionCropRequest q1Req = new QuestionCropRequest("1", 8.0, 1, 0.05, 0.1, 0.9, 0.35, null);
		QuestionDto q1 = examService.addQuestion(exam.id(), q1Req);
		assertNotNull(q1.id());
		assertEquals("1", q1.questionNumber());
		assertEquals(8.0, q1.maxScore());

		// Cannot complete segmentation if total score is not 20.0 yet
		assertThrows(DEIException.class, () -> examService.completeSegmentation(exam.id()));

		// Add Question 2
		QuestionCropRequest q2Req = new QuestionCropRequest("2", 12.0, 1, 0.05, 0.5, 0.9, 0.4, null);
		QuestionDto q2 = examService.addQuestion(exam.id(), q2Req);
		assertNotNull(q2.id());

		// Check exam questions list
		List<QuestionDto> questions = examService.getExamQuestions(exam.id());
		assertEquals(2, questions.size());

		// Complete segmentation
		ExamDto completedExam = examService.completeSegmentation(exam.id());
		assertEquals(ExamStatus.SEGMENTED.name(), completedExam.status());
		assertEquals(20.0, completedExam.totalScore());
	}

	@Test
	void testCannotCompleteEmptyExamSegmentation() {
		byte[] mockPdfBytes = generateSimplePdfBytes();
		MockMultipartFile multipartFile = new MockMultipartFile(
				"file",
				"empty-exam.pdf",
				"application/pdf",
				mockPdfBytes
		);

		ExamDto exam = examService.createExam(multipartFile, "Exame Vazio", school.id(), discipline.id(), student.id());
		assertThrows(DEIException.class, () -> examService.completeSegmentation(exam.id()));
	}

	private byte[] generateSimplePdfBytes() {
		try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
			org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
			doc.addPage(page);
			try (org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
				cs.beginText();
				cs.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
				cs.newLineAtOffset(100, 700);
				cs.showText("Exame de Teste");
				cs.endText();
			}
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			doc.save(baos);
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
