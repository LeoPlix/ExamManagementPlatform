package pt.ulisboa.tecnico.rnl.dei.ems.person.dto;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;

// password is WRITE_ONLY: accepted on create/update, never serialized back to client
public record PersonDto(
		Long id,
		String name,
		String email,
		@JsonProperty(access = Access.WRITE_ONLY) String password,
		String type,
		Long schoolId,
		String schoolName,
		Set<Long> disciplineIds,
		List<String> disciplineNames) {

	public PersonDto(Person person) {
		this(
				person.getId(),
				person.getName(),
				person.getEmail(),
				null,
				person.getType() != null ? person.getType().toString() : null,
				person.getSchool() != null ? person.getSchool().getId() : null,
				person.getSchool() != null ? person.getSchool().getName() : null,
				person.getDisciplines() != null
						? person.getDisciplines().stream().map(Discipline::getId).collect(Collectors.toSet())
						: Collections.emptySet(),
				person.getDisciplines() != null
						? person.getDisciplines().stream().map(Discipline::getName).toList()
						: Collections.emptyList()
		);
	}
}
