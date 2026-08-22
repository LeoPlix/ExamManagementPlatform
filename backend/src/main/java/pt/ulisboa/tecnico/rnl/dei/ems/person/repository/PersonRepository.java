package pt.ulisboa.tecnico.rnl.dei.ems.person.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;

@Repository
@Transactional
public interface PersonRepository extends JpaRepository<Person, Long> {

	Optional<Person> findByEmail(String email);

	boolean existsByEmail(String email);

	List<Person> findByType(PersonType type);

	List<Person> findByTypeAndDisciplinesId(PersonType type, Long disciplineId);

	List<Person> findByTypeAndSchoolId(PersonType type, Long schoolId);
}

