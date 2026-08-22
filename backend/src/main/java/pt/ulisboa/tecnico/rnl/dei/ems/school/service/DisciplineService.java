package pt.ulisboa.tecnico.rnl.dei.ems.school.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.Discipline;
import pt.ulisboa.tecnico.rnl.dei.ems.school.dto.DisciplineDto;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.DisciplineRepository;

@Service
@Transactional
public class DisciplineService {

	private final DisciplineRepository disciplineRepository;

	public DisciplineService(DisciplineRepository disciplineRepository) {
		this.disciplineRepository = disciplineRepository;
	}

	public Discipline fetchDisciplineOrThrow(long id) {
		return disciplineRepository.findById(id)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_DISCIPLINE, Long.toString(id)));
	}

	@Transactional(readOnly = true)
	public List<DisciplineDto> getDisciplines() {
		return disciplineRepository.findAll().stream()
				.map(DisciplineDto::new)
				.toList();
	}

	@Transactional(readOnly = true)
	public DisciplineDto getDiscipline(long id) {
		return new DisciplineDto(fetchDisciplineOrThrow(id));
	}

	public DisciplineDto createDiscipline(DisciplineDto disciplineDto) {
		if (disciplineDto.name() == null || disciplineDto.name().trim().isBlank()) {
			throw new DEIException(ErrorMessage.DISCIPLINE_NAME_NOT_VALID);
		}
		if (disciplineDto.code() == null || disciplineDto.code().trim().isBlank()) {
			throw new DEIException(ErrorMessage.DISCIPLINE_CODE_NOT_VALID);
		}

		String trimmedCode = disciplineDto.code().trim().toUpperCase();
		if (disciplineRepository.existsByCode(trimmedCode)) {
			throw new DEIException(ErrorMessage.DISCIPLINE_CODE_ALREADY_EXISTS, trimmedCode);
		}

		Discipline discipline = new Discipline(disciplineDto.name().trim(), trimmedCode);
		return new DisciplineDto(disciplineRepository.save(discipline));
	}

	public DisciplineDto updateDiscipline(long id, DisciplineDto disciplineDto) {
		Discipline discipline = fetchDisciplineOrThrow(id);

		if (disciplineDto.name() == null || disciplineDto.name().trim().isBlank()) {
			throw new DEIException(ErrorMessage.DISCIPLINE_NAME_NOT_VALID);
		}
		if (disciplineDto.code() == null || disciplineDto.code().trim().isBlank()) {
			throw new DEIException(ErrorMessage.DISCIPLINE_CODE_NOT_VALID);
		}

		String trimmedCode = disciplineDto.code().trim().toUpperCase();
		if (!trimmedCode.equalsIgnoreCase(discipline.getCode()) && disciplineRepository.existsByCode(trimmedCode)) {
			throw new DEIException(ErrorMessage.DISCIPLINE_CODE_ALREADY_EXISTS, trimmedCode);
		}

		discipline.setName(disciplineDto.name().trim());
		discipline.setCode(trimmedCode);

		return new DisciplineDto(disciplineRepository.save(discipline));
	}

	public void deleteDiscipline(long id) {
		fetchDisciplineOrThrow(id);
		disciplineRepository.deleteById(id);
	}
}
