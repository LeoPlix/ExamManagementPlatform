package pt.ulisboa.tecnico.rnl.dei.ems.exam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam;
import pt.ulisboa.tecnico.rnl.dei.ems.exam.domain.Exam.ExamStatus;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
	List<Exam> findBySchoolId(Long schoolId);
	List<Exam> findByDisciplineId(Long disciplineId);
	List<Exam> findByStudentId(Long studentId);
	List<Exam> findByStatus(ExamStatus status);
	List<Exam> findBySchoolIdAndDisciplineId(Long schoolId, Long disciplineId);
	List<Exam> findByDisciplineIdAndStatus(Long disciplineId, ExamStatus status);
	List<Exam> findBySchoolIdAndStatus(Long schoolId, ExamStatus status);
	List<Exam> findBySchoolIdAndDisciplineIdAndStatus(Long schoolId, Long disciplineId, ExamStatus status);
	List<Exam> findByStudentIdAndReleasedTrue(Long studentId);
	boolean existsByStudentIdAndDisciplineId(Long studentId, Long disciplineId);
	boolean existsByDisciplineIdAndStatusIn(Long disciplineId, List<ExamStatus> statuses);
	boolean existsBySchoolIdAndStatusIn(Long schoolId, List<ExamStatus> statuses);
	boolean existsByStatusIn(List<ExamStatus> statuses);
}

