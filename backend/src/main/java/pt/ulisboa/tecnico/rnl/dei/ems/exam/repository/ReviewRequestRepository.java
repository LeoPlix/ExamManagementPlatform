package pt.ulisboa.tecnico.rnl.dei.ems.exam.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.ReviewRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.ReviewRequest.ReviewStatus;

@Repository
public interface ReviewRequestRepository extends JpaRepository<ReviewRequest, Long> {
	List<ReviewRequest> findByStudentId(Long studentId);
	List<ReviewRequest> findByReviewerId(Long reviewerId);
	List<ReviewRequest> findByReviewerIdAndStatus(Long reviewerId, ReviewStatus status);
	long countByReviewerIdAndStatus(Long reviewerId, ReviewStatus status);
	List<ReviewRequest> findByStatus(ReviewStatus status);
	Optional<ReviewRequest> findByQuestionIdAndStudentId(Long questionId, Long studentId);
}
