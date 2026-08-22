package pt.ulisboa.tecnico.rnl.dei.ems.auth.dto;

import java.util.List;

public record AuthUserDto(
		long id,
		String name,
		String email,
		String role,
		Long schoolId,
		String schoolName,
		List<String> permissions) {
}
