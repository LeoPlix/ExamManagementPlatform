package pt.ulisboa.tecnico.rnl.dei.ems.school.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {
	Optional<Discipline> findByCode(String code);
	boolean existsByCode(String code);
}
