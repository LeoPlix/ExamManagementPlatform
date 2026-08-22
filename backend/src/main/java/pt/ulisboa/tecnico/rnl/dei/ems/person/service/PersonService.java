package pt.ulisboa.tecnico.rnl.dei.ems.person.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person.PersonType;
import pt.ulisboa.tecnico.rnl.dei.ems.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.ems.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.School;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.DisciplineRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.SchoolRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.security.SecurityUtils;

@Service
@Transactional
public class PersonService {

	private final PersonRepository personRepository;
	private final SchoolRepository schoolRepository;
	private final DisciplineRepository disciplineRepository;
	private final PasswordEncoder passwordEncoder;

	public PersonService(
			PersonRepository personRepository,
			SchoolRepository schoolRepository,
			DisciplineRepository disciplineRepository,
			PasswordEncoder passwordEncoder) {
		this.personRepository = personRepository;
		this.schoolRepository = schoolRepository;
		this.disciplineRepository = disciplineRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Person fetchPersonOrThrow(long id) {
		return personRepository.findById(id)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, Long.toString(id)));
	}

	@Transactional(readOnly = true)
	public List<PersonDto> getPeople() {
		return personRepository.findAll().stream()
				.map(PersonDto::new)
				.toList();
	}

	@Transactional(readOnly = true)
	public PersonDto getPerson(long id) {
		return new PersonDto(fetchPersonOrThrow(id));
	}

	public PersonDto createPerson(PersonDto personDto) {
		validatePersonDto(personDto, true);

		PersonType type = parsePersonType(personDto.type());
		// Administrators cannot create other administrators
		if (type == PersonType.ADMINISTRATOR) {
			throw new DEIException(ErrorMessage.ACCESS_DENIED);
		}

		String email = personDto.email().trim().toLowerCase();
		if (personRepository.existsByEmail(email)) {
			throw new DEIException(ErrorMessage.EMAIL_ALREADY_EXISTS, email);
		}

		Person person = new Person(
				personDto.name().trim(),
				email,
				passwordEncoder.encode(personDto.password().trim()),
				type);

		applySchoolAndDisciplines(person, personDto);

		return new PersonDto(personRepository.save(person));
	}

	public PersonDto updatePerson(long id, PersonDto personDto) {
		Person person = fetchPersonOrThrow(id);
		validatePersonDto(personDto, false);

		// An administrator cannot edit themselves or other administrator accounts
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getId() != null && current.getId() == id) {
			throw new DEIException(ErrorMessage.ACCESS_DENIED);
		}
		if (person.getType() == PersonType.ADMINISTRATOR) {
			throw new DEIException(ErrorMessage.ACCESS_DENIED);
		}

		PersonType newType = parsePersonType(personDto.type());
		if (newType == PersonType.ADMINISTRATOR) {
			throw new DEIException(ErrorMessage.ACCESS_DENIED);
		}

		String email = personDto.email().trim().toLowerCase();
		if (!email.equalsIgnoreCase(person.getEmail()) && personRepository.existsByEmail(email)) {
			throw new DEIException(ErrorMessage.EMAIL_ALREADY_EXISTS, email);
		}

		person.setName(personDto.name().trim());
		person.setEmail(email);
		person.setType(newType);

		if (personDto.password() != null && !personDto.password().isBlank()) {
			person.setPassword(passwordEncoder.encode(personDto.password().trim()));
		}

		applySchoolAndDisciplines(person, personDto);

		return new PersonDto(personRepository.save(person));
	}

	public void deletePerson(long id) {
		Person person = fetchPersonOrThrow(id);

		// Cannot delete self or administrator accounts
		Person current = SecurityUtils.currentPerson();
		if (current != null && current.getId() != null && current.getId() == id) {
			throw new DEIException(ErrorMessage.ACCESS_DENIED);
		}
		if (person.getType() == PersonType.ADMINISTRATOR) {
			throw new DEIException(ErrorMessage.ACCESS_DENIED);
		}

		personRepository.deleteById(id);
	}

	private void validatePersonDto(PersonDto dto, boolean requirePassword) {
		if (dto.name() == null || dto.name().isBlank()) {
			throw new DEIException(ErrorMessage.PERSON_NAME_NOT_VALID);
		}
		if (dto.email() == null || dto.email().isBlank() || !dto.email().contains("@")) {
			throw new DEIException(ErrorMessage.PERSON_EMAIL_NOT_VALID);
		}
		if (requirePassword && (dto.password() == null || dto.password().trim().length() < 4)) {
			throw new DEIException(ErrorMessage.PERSON_PASSWORD_NOT_VALID);
		}
	}

	private PersonType parsePersonType(String typeStr) {
		try {
			return PersonType.valueOf(typeStr.trim().toUpperCase());
		} catch (Exception e) {
			throw new DEIException(ErrorMessage.ACCESS_DENIED);
		}
	}

	private void applySchoolAndDisciplines(Person person, PersonDto personDto) {
		if (personDto.schoolId() != null) {
			School school = schoolRepository.findById(personDto.schoolId())
					.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_SCHOOL, Long.toString(personDto.schoolId())));
			person.setSchool(school);
		} else {
			person.setSchool(null);
		}

		if (personDto.disciplineIds() != null && !personDto.disciplineIds().isEmpty()) {
			Set<Discipline> disciplines = new HashSet<>(disciplineRepository.findAllById(personDto.disciplineIds()));
			person.setDisciplines(disciplines);
		} else {
			person.getDisciplines().clear();
		}
	}
}
