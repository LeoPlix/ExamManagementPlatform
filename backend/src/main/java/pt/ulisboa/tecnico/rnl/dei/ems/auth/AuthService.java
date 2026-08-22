package pt.ulisboa.tecnico.rnl.dei.ems.auth;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.auth.dto.AuthUserDto;
import pt.ulisboa.tecnico.rnl.dei.ems.auth.dto.LoginRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.auth.dto.LoginResponse;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.ems.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.ems.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.ems.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.ems.security.JwtService;
import pt.ulisboa.tecnico.rnl.dei.ems.security.Permission;
import pt.ulisboa.tecnico.rnl.dei.ems.security.RolePermissions;
import pt.ulisboa.tecnico.rnl.dei.ems.security.SecurityUtils;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final PersonRepository personRepository;

	public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
			PersonRepository personRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.personRepository = personRepository;
	}

	public LoginResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password()));

		Person person = personRepository.findByEmail(request.email())
				.orElseThrow(() -> new DEIException(ErrorMessage.INVALID_CREDENTIALS));

		String token = jwtService.generateToken(person);
		return new LoginResponse(token, jwtService.getExpirationMs(), toDto(person));
	}

	public AuthUserDto currentUser(HttpServletRequest request) {
		Person person = SecurityUtils.currentPerson();
		if (person == null) {
			throw new DEIException(ErrorMessage.NOT_AUTHENTICATED);
		}
		return toDto(person);
	}

	private AuthUserDto toDto(Person person) {
		List<String> permissions = RolePermissions.forRole(person.getType()).stream()
				.map(Permission::name)
				.sorted()
				.toList();

		Long schoolId = person.getSchool() != null ? person.getSchool().getId() : null;
		String schoolName = person.getSchool() != null ? person.getSchool().getName() : null;

		return new AuthUserDto(
				person.getId(),
				person.getName(),
				person.getEmail(),
				person.getType().name(),
				schoolId,
				schoolName,
				permissions);
	}
}
