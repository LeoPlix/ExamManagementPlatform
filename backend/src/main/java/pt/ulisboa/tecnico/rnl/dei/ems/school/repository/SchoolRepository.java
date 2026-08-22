package pt.ulisboa.tecnico.rnl.dei.ems.school.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.School;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
	Optional<School> findByCode(String code);
	boolean existsByCode(String code);
}
