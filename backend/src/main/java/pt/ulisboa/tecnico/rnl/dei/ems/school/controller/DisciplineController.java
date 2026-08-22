package pt.ulisboa.tecnico.rnl.dei.ems.school.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.ems.school.dto.DisciplineDto;
import pt.ulisboa.tecnico.rnl.dei.ems.school.service.DisciplineService;

@RestController
@RequestMapping("/disciplines")
public class DisciplineController {

	private final DisciplineService disciplineService;

	public DisciplineController(DisciplineService disciplineService) {
		this.disciplineService = disciplineService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('DISCIPLINE_READ')")
	public List<DisciplineDto> getDisciplines() {
		return disciplineService.getDisciplines();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('DISCIPLINE_READ')")
	public DisciplineDto getDiscipline(@PathVariable long id) {
		return disciplineService.getDiscipline(id);
	}

	@PostMapping
	@PreAuthorize("hasAuthority('DISCIPLINE_CREATE')")
	public DisciplineDto createDiscipline(@RequestBody DisciplineDto disciplineDto) {
		return disciplineService.createDiscipline(disciplineDto);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('DISCIPLINE_UPDATE')")
	public DisciplineDto updateDiscipline(@PathVariable long id, @RequestBody DisciplineDto disciplineDto) {
		return disciplineService.updateDiscipline(id, disciplineDto);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('DISCIPLINE_DELETE')")
	public void deleteDiscipline(@PathVariable long id) {
		disciplineService.deleteDiscipline(id);
	}
}
