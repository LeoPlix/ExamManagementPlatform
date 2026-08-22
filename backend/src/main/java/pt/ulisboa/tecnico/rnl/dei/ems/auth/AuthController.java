package pt.ulisboa.tecnico.rnl.dei.ems.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import pt.ulisboa.tecnico.rnl.dei.ems.auth.dto.AuthUserDto;
import pt.ulisboa.tecnico.rnl.dei.ems.auth.dto.LoginRequest;
import pt.ulisboa.tecnico.rnl.dei.ems.auth.dto.LoginResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public AuthUserDto me(HttpServletRequest request) {
		return authService.currentUser(request);
	}
}
