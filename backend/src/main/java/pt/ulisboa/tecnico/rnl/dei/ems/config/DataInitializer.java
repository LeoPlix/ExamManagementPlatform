package pt.ulisboa.tecnico.rnl.dei.ems.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.repository.ExamRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.service.FileStorageService;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.ems.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.School;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.DisciplineRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.SchoolRepository;

@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	private final PersonRepository personRepository;
	private final SchoolRepository schoolRepository;
	private final DisciplineRepository disciplineRepository;
	private final ExamRepository examRepository;
	private final FileStorageService fileStorageService;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(
			PersonRepository personRepository,
			SchoolRepository schoolRepository,
			DisciplineRepository disciplineRepository,
			ExamRepository examRepository,
			FileStorageService fileStorageService,
			PasswordEncoder passwordEncoder) {
		this.personRepository = personRepository;
		this.schoolRepository = schoolRepository;
		this.disciplineRepository = disciplineRepository;
		this.examRepository = examRepository;
		this.fileStorageService = fileStorageService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		// 1. Schools
		School camoes = seedSchool("Escola Secundária de Camões", "ESC-CAM", "Lisboa e Vale do Tejo");
		School filipa = seedSchool("Escola Secundária D. Filipa de Lencastre", "ESC-DFL", "Lisboa e Vale do Tejo");

		// 2. Disciplines
		Discipline matA = seedDiscipline("Matemática A", "MAT-A");
		Discipline fqa = seedDiscipline("Física e Química A", "FQA");
		Discipline port = seedDiscipline("Português", "PORT");
		Discipline bg = seedDiscipline("Biologia e Geologia", "BG");

		// 3. Administrator
		seedPerson("Administrador Geral", "admin@dei.tecnico.ulisboa.pt", "admin123", PersonType.ADMINISTRATOR, null, null);

		// 4. School Staff
		Person staffCamoes = seedPerson("Funcionário Camões", "staff.camoes@dei.tecnico.ulisboa.pt", "staff123", PersonType.SCHOOL_STAFF, camoes, null);
		seedPerson("Funcionário Filipa", "staff.filipa@dei.tecnico.ulisboa.pt", "staff123", PersonType.SCHOOL_STAFF, filipa, null);

		// 5. Teachers
		seedPerson("Prof. António Matemática", "prof.mat@dei.tecnico.ulisboa.pt", "teacher123", PersonType.TEACHER, camoes, Set.of(matA));
		seedPerson("Prof. Beatriz Física", "prof.fqa@dei.tecnico.ulisboa.pt", "teacher123", PersonType.TEACHER, filipa, Set.of(fqa));
		seedPerson("Prof. Carlos Português", "prof.port@dei.tecnico.ulisboa.pt", "teacher123", PersonType.TEACHER, camoes, Set.of(port));

		// 6. Students (10 students)
		Person student1 = seedPerson("Ana Silva", "aluno1@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, camoes, null);
		seedPerson("Bernardo Santos", "aluno2@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, camoes, null);
		seedPerson("Catarina Martins", "aluno3@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, camoes, null);
		seedPerson("Diogo Ferreira", "aluno4@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, camoes, null);
		seedPerson("Eduardo Costa", "aluno5@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, camoes, null);
		seedPerson("Francisca Oliveira", "aluno6@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, filipa, null);
		seedPerson("Gonçalo Pereira", "aluno7@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, filipa, null);
		seedPerson("Helena Rodrigues", "aluno8@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, filipa, null);
		seedPerson("Inês Sousa", "aluno9@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, filipa, null);
		seedPerson("João Carvalho", "aluno10@dei.tecnico.ulisboa.pt", "student123", PersonType.STUDENT, filipa, null);

		// 7. Seed sample exam PDF for immediate validation of Phase 1
		seedSampleExam(camoes, matA, student1);
	}

	private School seedSchool(String name, String code, String region) {
		return schoolRepository.findByCode(code).orElseGet(() -> {
			School s = schoolRepository.save(new School(name, code, region));
			logger.info("Seeded School: {} ({})", name, code);
			return s;
		});
	}

	private Discipline seedDiscipline(String name, String code) {
		return disciplineRepository.findByCode(code).orElseGet(() -> {
			Discipline d = disciplineRepository.save(new Discipline(name, code));
			logger.info("Seeded Discipline: {} ({})", name, code);
			return d;
		});
	}

	private Person seedPerson(String name, String email, String rawPassword, PersonType type, School school, Set<Discipline> disciplines) {
		return personRepository.findByEmail(email).orElseGet(() -> {
			Person p = new Person(name, email, passwordEncoder.encode(rawPassword), type, school);
			if (disciplines != null) {
				p.getDisciplines().addAll(disciplines);
			}
			Person saved = personRepository.save(p);
			logger.info("Seeded Account: {} ({})", email, type);
			return saved;
		});
	}

	private void seedSampleExam(School school, Discipline discipline, Person student) {
		if (!examRepository.findAll().isEmpty()) {
			return;
		}

		try {
			// Generate a neat multi-page sample exam PDF
			String demoPdfPath = "./uploads/exams/demo-exam-mat-a.pdf";
			Files.createDirectories(Paths.get("./uploads/exams"));
			File demoFile = new File(demoPdfPath);

			if (!demoFile.exists()) {
				try (PDDocument doc = new PDDocument()) {
					// Page 1
					PDPage page1 = new PDPage(PDRectangle.A4);
					doc.addPage(page1);
					try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
						cs.beginText();
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
						cs.newLineAtOffset(50, 780);
						cs.showText("EduQa-nos - Exame Nacional de Matematica A (635)");
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
						cs.newLineAtOffset(0, -30);
						cs.showText("Escola: " + school.getName() + " | Prova 635 - 1a Fase");
						cs.newLineAtOffset(0, -20);
						cs.showText("Aluno: " + student.getName() + " (" + student.getEmail() + ")");
						cs.newLineAtOffset(0, -40);

						cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
						cs.showText("Item 1. (Cotacao: 2.5 valores)");
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
						cs.newLineAtOffset(0, -25);
						cs.showText("Considere a funcao f, de dominio R, definida por f(x) = e^(2x) - 3x.");
						cs.newLineAtOffset(0, -20);
						cs.showText("Determine a equacao da reta tangente ao grafico de f no ponto de abcissa x = 0.");
						cs.newLineAtOffset(0, -35);
						cs.showText("Resolucao do Aluno:");
						cs.newLineAtOffset(0, -20);
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 11);
						cs.showText("f'(x) = 2*e^(2x) - 3 => f'(0) = 2*(1) - 3 = -1.");
						cs.newLineAtOffset(0, -18);
						cs.showText("f(0) = e^0 - 0 = 1. Ponto (0, 1), declive m = -1.");
						cs.newLineAtOffset(0, -18);
						cs.showText("Equacao: y - 1 = -1(x - 0) <=> y = -x + 1.");

						cs.newLineAtOffset(0, -50);
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
						cs.showText("Item 2. (Cotacao: 3.0 valores)");
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
						cs.newLineAtOffset(0, -25);
						cs.showText("Seja z = 1 + i*sqrt(3) um numero complexo.");
						cs.newLineAtOffset(0, -20);
						cs.showText("Escreva z na forma trigonometrica e calcule z^6.");
						cs.newLineAtOffset(0, -35);
						cs.showText("Resolucao do Aluno:");
						cs.newLineAtOffset(0, -20);
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 11);
						cs.showText("|z| = sqrt(1 + 3) = 2, arg(z) = pi/3.");
						cs.newLineAtOffset(0, -18);
						cs.showText("z = 2*cis(pi/3) => z^6 = 2^6 * cis(6*pi/3) = 64*cis(2pi) = 64.");
						cs.endText();
					}

					// Page 2
					PDPage page2 = new PDPage(PDRectangle.A4);
					doc.addPage(page2);
					try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
						cs.beginText();
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
						cs.newLineAtOffset(50, 780);
						cs.showText("Item 3. (Cotacao: 3.5 valores)");
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
						cs.newLineAtOffset(0, -25);
						cs.showText("Num saco estao 5 bolas brancas e 4 bolas pretas.");
						cs.newLineAtOffset(0, -20);
						cs.showText("Retiram-se, simultaneamente e ao acaso, 3 bolas do saco.");
						cs.newLineAtOffset(0, -20);
						cs.showText("Calcule a probabilidade de sairem pelo menos 2 bolas brancas.");
						cs.newLineAtOffset(0, -35);
						cs.showText("Resolucao do Aluno:");
						cs.newLineAtOffset(0, -20);
						cs.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 11);
						cs.showText("Casos possiveis = 9C3 = 84.");
						cs.newLineAtOffset(0, -18);
						cs.showText("Casos favoraveis (2 brancas + 1 preta) = 5C2 * 4C1 = 10 * 4 = 40.");
						cs.newLineAtOffset(0, -18);
						cs.showText("Casos favoraveis (3 brancas) = 5C3 = 10.");
						cs.newLineAtOffset(0, -18);
						cs.showText("P = (40 + 10) / 84 = 50 / 84 = 25 / 42.");
						cs.endText();
					}

					doc.save(demoFile);
				}
			}

			Exam exam = new Exam(
					"Exame Nacional Matemática A - 2026",
					school,
					discipline,
					student,
					demoPdfPath,
					"exame-matematica-a-2026.pdf",
					2
			);
			exam.setStatus(ExamStatus.UPLOADED);
			examRepository.save(exam);
			logger.info("Seeded demo exam PDF for Phase 1 testing.");

		} catch (IOException e) {
			logger.warn("Could not seed sample PDF: {}", e.getMessage());
		}
	}
}
