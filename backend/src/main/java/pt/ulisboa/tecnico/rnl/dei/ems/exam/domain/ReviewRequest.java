package pt.ulisboa.tecnico.rnl.dei.ems.exam.domain;

import java.time.LocalDateTime;

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

@Entity
@Table(name = "review_requests")
public class ReviewRequest {

	public enum ReviewStatus {
		PENDING,
		ASSIGNED,
		RESOLVED,
		REJECTED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "student_id", nullable = false)
	private Person student;

	@Column(name = "justification", nullable = false, columnDefinition = "TEXT")
	private String justification;

	@Column(name = "original_score")
	private Double originalScore;

	@Column(name = "revised_score")
	private Double revisedScore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id")
	private Person reviewer;

	@Column(name = "reviewer_feedback", columnDefinition = "TEXT")
	private String reviewerFeedback;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ReviewStatus status = ReviewStatus.PENDING;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "resolved_at")
	private LocalDateTime resolvedAt;

	public ReviewRequest() {
	}

	public ReviewRequest(Question question, Person student, String justification, Double originalScore) {
		this.question = question;
		this.student = student;
		this.justification = justification;
		this.originalScore = originalScore;
		this.status = ReviewStatus.PENDING;
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Person getStudent() {
		return student;
	}

	public void setStudent(Person student) {
		this.student = student;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public Double getOriginalScore() {
		return originalScore;
	}

	public void setOriginalScore(Double originalScore) {
		this.originalScore = originalScore;
	}

	public Double getRevisedScore() {
		return revisedScore;
	}

	public void setRevisedScore(Double revisedScore) {
		this.revisedScore = revisedScore;
	}

	public Person getReviewer() {
		return reviewer;
	}

	public void setReviewer(Person reviewer) {
		this.reviewer = reviewer;
	}

	public String getReviewerFeedback() {
		return reviewerFeedback;
	}

	public void setReviewerFeedback(String reviewerFeedback) {
		this.reviewerFeedback = reviewerFeedback;
	}

	public ReviewStatus getStatus() {
		return status;
	}

	public void setStatus(ReviewStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(LocalDateTime resolvedAt) {
		this.resolvedAt = resolvedAt;
	}
}
