package pt.ulisboa.tecnico.rnl.dei.ems.exam.domain;

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
import jakarta.persistence.Table;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;

@Entity
@Table(name = "questions")
public class Question {

	public enum QuestionStatus {
		PENDING_DISTRIBUTION,
		PENDING_EVALUATION,
		EVALUATED,
		IN_REVIEW,
		REVIEWED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id", nullable = false)
	private Exam exam;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "discipline_id", nullable = false)
	private Discipline discipline;

	@Column(name = "question_number", nullable = false)
	private String questionNumber;

	@Column(name = "max_score", nullable = false)
	private Double maxScore;

	@Column(name = "image_path", nullable = false)
	private String imagePath;

	@Column(name = "page_number", nullable = false)
	private int pageNumber;

	@Column(name = "crop_x")
	private Double cropX;

	@Column(name = "crop_y")
	private Double cropY;

	@Column(name = "crop_width")
	private Double cropWidth;

	@Column(name = "crop_height")
	private Double cropHeight;

	@Column(name = "order_index", nullable = false)
	private int orderIndex = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "evaluator_id")
	private Person evaluator;

	@Column(name = "score")
	private Double score;

	@Column(name = "feedback", columnDefinition = "TEXT")
	private String feedback;

	@Column(name = "annotated_image_path")
	private String annotatedImagePath;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private QuestionStatus status = QuestionStatus.PENDING_DISTRIBUTION;

	public Question() {
	}

	public Question(Exam exam, Discipline discipline, String questionNumber, Double maxScore,
			String imagePath, int pageNumber, Double cropX, Double cropY, Double cropWidth, Double cropHeight, int orderIndex) {
		this.exam = exam;
		this.discipline = discipline;
		this.questionNumber = questionNumber;
		this.maxScore = maxScore;
		this.imagePath = imagePath;
		this.pageNumber = pageNumber;
		this.cropX = cropX;
		this.cropY = cropY;
		this.cropWidth = cropWidth;
		this.cropHeight = cropHeight;
		this.orderIndex = orderIndex;
		this.status = QuestionStatus.PENDING_DISTRIBUTION;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Exam getExam() {
		return exam;
	}

	public void setExam(Exam exam) {
		this.exam = exam;
	}

	public Discipline getDiscipline() {
		return discipline;
	}

	public void setDiscipline(Discipline discipline) {
		this.discipline = discipline;
	}

	public String getQuestionNumber() {
		return questionNumber;
	}

	public void setQuestionNumber(String questionNumber) {
		this.questionNumber = questionNumber;
	}

	public Double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Double maxScore) {
		this.maxScore = maxScore;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Double getCropX() {
		return cropX;
	}

	public void setCropX(Double cropX) {
		this.cropX = cropX;
	}

	public Double getCropY() {
		return cropY;
	}

	public void setCropY(Double cropY) {
		this.cropY = cropY;
	}

	public Double getCropWidth() {
		return cropWidth;
	}

	public void setCropWidth(Double cropWidth) {
		this.cropWidth = cropWidth;
	}

	public Double getCropHeight() {
		return cropHeight;
	}

	public void setCropHeight(Double cropHeight) {
		this.cropHeight = cropHeight;
	}

	public int getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}

	public Person getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(Person evaluator) {
		this.evaluator = evaluator;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public String getAnnotatedImagePath() {
		return annotatedImagePath;
	}

	public void setAnnotatedImagePath(String annotatedImagePath) {
		this.annotatedImagePath = annotatedImagePath;
	}

	public QuestionStatus getStatus() {
		return status;
	}

	public void setStatus(QuestionStatus status) {
		this.status = status;
	}
}
