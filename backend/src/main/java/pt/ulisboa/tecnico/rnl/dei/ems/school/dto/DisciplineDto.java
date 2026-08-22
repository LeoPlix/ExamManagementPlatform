package pt.ulisboa.tecnico.rnl.dei.ems.school.dto;

import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;

public record DisciplineDto(
		Long id,
		String name,
		String code) {

	public DisciplineDto(Discipline discipline) {
		this(discipline.getId(), discipline.getName(), discipline.getCode());
	}
}
