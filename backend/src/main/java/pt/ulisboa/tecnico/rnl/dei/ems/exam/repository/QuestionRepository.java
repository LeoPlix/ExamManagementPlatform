package pt.ulisboa.tecnico.rnl.dei.ems.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
	List<Question> findByExamIdOrderByOrderIndexAsc(Long examId);
	List<Question> findByDisciplineId(Long disciplineId);
	List<Question> findByEvaluatorId(Long evaluatorId);
	List<Question> findByEvaluatorIdAndScoreIsNull(Long evaluatorId);
	List<Question> findByEvaluatorIdAndScoreIsNotNull(Long evaluatorId);
	List<Question> findByDisciplineIdAndEvaluatorIsNull(Long disciplineId);
	long countByEvaluatorId(Long evaluatorId);
	long countByEvaluatorIdAndScoreIsNull(Long evaluatorId);
}

