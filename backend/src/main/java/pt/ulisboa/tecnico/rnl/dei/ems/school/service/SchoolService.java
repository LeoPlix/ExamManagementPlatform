package pt.ulisboa.tecnico.rnl.dei.ems.school.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.school.domain.School;
import pt.ulisboa.tecnico.rnl.dei.ems.school.dto.SchoolDto;
import pt.ulisboa.tecnico.rnl.dei.ems.school.repository.SchoolRepository;

@Service
@Transactional
public class SchoolService {

	private final SchoolRepository schoolRepository;

	public SchoolService(SchoolRepository schoolRepository) {
		this.schoolRepository = schoolRepository;
	}

	public School fetchSchoolOrThrow(long id) {
		return schoolRepository.findById(id)
				.orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_SCHOOL, Long.toString(id)));
	}

	@Transactional(readOnly = true)
	public List<SchoolDto> getSchools() {
		return schoolRepository.findAll().stream()
				.map(SchoolDto::new)
				.toList();
	}

	@Transactional(readOnly = true)
	public SchoolDto getSchool(long id) {
		return new SchoolDto(fetchSchoolOrThrow(id));
	}

	public SchoolDto createSchool(SchoolDto schoolDto) {
		if (schoolDto.name() == null || schoolDto.name().trim().isBlank()) {
			throw new DEIException(ErrorMessage.SCHOOL_NAME_NOT_VALID);
		}
		if (schoolDto.code() == null || schoolDto.code().trim().isBlank()) {
			throw new DEIException(ErrorMessage.SCHOOL_CODE_NOT_VALID);
		}
		if (schoolDto.region() == null || schoolDto.region().trim().isBlank()) {
			throw new DEIException(ErrorMessage.SCHOOL_REGION_NOT_VALID);
		}

		String trimmedCode = schoolDto.code().trim().toUpperCase();
		if (schoolRepository.existsByCode(trimmedCode)) {
			throw new DEIException(ErrorMessage.SCHOOL_CODE_ALREADY_EXISTS, trimmedCode);
		}

		School school = new School(schoolDto.name().trim(), trimmedCode, schoolDto.region().trim());
		return new SchoolDto(schoolRepository.save(school));
	}

	public SchoolDto updateSchool(long id, SchoolDto schoolDto) {
		School school = fetchSchoolOrThrow(id);

		if (schoolDto.name() == null || schoolDto.name().trim().isBlank()) {
			throw new DEIException(ErrorMessage.SCHOOL_NAME_NOT_VALID);
		}
		if (schoolDto.code() == null || schoolDto.code().trim().isBlank()) {
			throw new DEIException(ErrorMessage.SCHOOL_CODE_NOT_VALID);
		}
		if (schoolDto.region() == null || schoolDto.region().trim().isBlank()) {
			throw new DEIException(ErrorMessage.SCHOOL_REGION_NOT_VALID);
		}

		String trimmedCode = schoolDto.code().trim().toUpperCase();
		if (!trimmedCode.equalsIgnoreCase(school.getCode()) && schoolRepository.existsByCode(trimmedCode)) {
			throw new DEIException(ErrorMessage.SCHOOL_CODE_ALREADY_EXISTS, trimmedCode);
		}

		school.setName(schoolDto.name().trim());
		school.setCode(trimmedCode);
		school.setRegion(schoolDto.region().trim());

		return new SchoolDto(schoolRepository.save(school));
	}

	public void deleteSchool(long id) {
		fetchSchoolOrThrow(id);
		schoolRepository.deleteById(id);
	}
}
