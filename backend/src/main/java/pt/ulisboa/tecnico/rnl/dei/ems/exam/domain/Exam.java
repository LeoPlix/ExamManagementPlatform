package pt.ulisboa.tecnico.rnl.dei.ems.exam.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.School;

@Entity
@Table(name = "exams")
public class Exam {

	public enum ExamStatus {
		UPLOADED,
		SEGMENTED,
		IN_DISTRIBUTION,
		DISTRIBUTED,
		CORRECTED,
		RELEASED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "school_id", nullable = false)
	private School school;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "discipline_id", nullable = false)
	private Discipline discipline;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "student_id", nullable = false)
	private Person student;

	@Column(name = "pdf_path", nullable = false)
	private String pdfPath;

	@Column(name = "pdf_filename", nullable = false)
	private String pdfFilename;

	@Column(name = "total_pages", nullable = false)
	private int totalPages = 1;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ExamStatus status = ExamStatus.UPLOADED;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "total_score")
	private Double totalScore = 0.0;

	@Column(name = "obtained_score")
	private Double obtainedScore = 0.0;

	@Column(name = "grades_published", nullable = false)
	private boolean gradesPublished = false;

	@Column(name = "grades_published_at")
	private LocalDateTime gradesPublishedAt;

	@Column(name = "is_released", nullable = false)
	private boolean released = false;

	@Column(name = "released_at")
	private LocalDateTime releasedAt;

	@Column(name = "review_deadline")
	private LocalDateTime reviewDeadline;

	@Column(name = "view_requested", nullable = false)
	private boolean viewRequested = false;

	@Column(name = "view_requested_at")
	private LocalDateTime viewRequestedAt;

	@OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orderIndex ASC")
	private List<Question> questions = new ArrayList<>();

	public Exam() {
	}

	public Exam(String title, School school, Discipline discipline, Person student, String pdfPath, String pdfFilename, int totalPages) {
		this.title = title;
		this.school = school;
		this.discipline = discipline;
		this.student = student;
		this.pdfPath = pdfPath;
		this.pdfFilename = pdfFilename;
		this.totalPages = totalPages;
		this.status = ExamStatus.UPLOADED;
		this.createdAt = LocalDateTime.now();
		this.totalScore = 0.0;
		this.obtainedScore = 0.0;
		this.released = false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = school;
	}

	public Discipline getDiscipline() {
		return discipline;
	}

	public void setDiscipline(Discipline discipline) {
		this.discipline = discipline;
	}

	public Person getStudent() {
		return student;
	}

	public void setStudent(Person student) {
		this.student = student;
	}

	public String getPdfPath() {
		return pdfPath;
	}

	public void setPdfPath(String pdfPath) {
		this.pdfPath = pdfPath;
	}

	public String getPdfFilename() {
		return pdfFilename;
	}

	public void setPdfFilename(String pdfFilename) {
		this.pdfFilename = pdfFilename;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public ExamStatus getStatus() {
		return status;
	}

	public void setStatus(ExamStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Double totalScore) {
		this.totalScore = totalScore;
	}

	public Double getObtainedScore() {
		return obtainedScore;
	}

	public void setObtainedScore(Double obtainedScore) {
		this.obtainedScore = obtainedScore;
	}

	public boolean isGradesPublished() {
		return gradesPublished;
	}

	public void setGradesPublished(boolean gradesPublished) {
		this.gradesPublished = gradesPublished;
	}

	public LocalDateTime getGradesPublishedAt() {
		return gradesPublishedAt;
	}

	public void setGradesPublishedAt(LocalDateTime gradesPublishedAt) {
		this.gradesPublishedAt = gradesPublishedAt;
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	public LocalDateTime getReleasedAt() {
		return releasedAt;
	}

	public void setReleasedAt(LocalDateTime releasedAt) {
		this.releasedAt = releasedAt;
	}

	public LocalDateTime getReviewDeadline() {
		return reviewDeadline;
	}

	public void setReviewDeadline(LocalDateTime reviewDeadline) {
		this.reviewDeadline = reviewDeadline;
	}

	public boolean isViewRequested() {
		return viewRequested;
	}

	public void setViewRequested(boolean viewRequested) {
		this.viewRequested = viewRequested;
	}

	public LocalDateTime getViewRequestedAt() {
		return viewRequestedAt;
	}

	public void setViewRequestedAt(LocalDateTime viewRequestedAt) {
		this.viewRequestedAt = viewRequestedAt;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public void calculateTotalScore() {
		if (this.questions == null || this.questions.isEmpty()) {
			this.totalScore = 0.0;
		} else {
			this.totalScore = this.questions.stream()
					.mapToDouble(q -> q.getMaxScore() != null ? q.getMaxScore() : 0.0)
					.sum();
		}
	}

	public void calculateObtainedScore() {
		if (this.questions == null || this.questions.isEmpty()) {
			this.obtainedScore = 0.0;
		} else {
			this.obtainedScore = this.questions.stream()
					.filter(q -> q.getScore() != null)
					.mapToDouble(Question::getScore)
					.sum();
		}
	}

	public boolean isFullyEvaluated() {
		if (this.questions == null || this.questions.isEmpty()) {
			return false;
		}
		return this.questions.stream().allMatch(q -> q.getScore() != null);
	}
}
