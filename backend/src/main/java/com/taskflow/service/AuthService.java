package com.taskflow.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.taskflow.dto.RegistrationRequest;
import com.taskflow.dto.RegistrationResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.LoginResponse;
import com.taskflow.dto.AuthenticatedUserResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.DuplicateEmailException;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Transactional
	public RegistrationResponse register(@Valid RegistrationRequest registrationRequest) {
		String normalizedEmail = normalizeEmail(registrationRequest.email());

		if (userRepository.findByEmail(normalizedEmail).isPresent()) {
			throw new DuplicateEmailException();
		}

		User user = new User();
		user.setName(registrationRequest.name().strip());
		user.setEmail(normalizedEmail);
		user.setPassword(passwordEncoder.encode(registrationRequest.password()));

		User savedUser = userRepository.save(user);
		return new RegistrationResponse(
				savedUser.getId(),
				savedUser.getName(),
				savedUser.getEmail(),
				savedUser.getCreatedAt());
	}

	@Transactional(readOnly = true)
	public LoginResponse login(@Valid LoginRequest loginRequest) {
		String normalizedEmail = normalizeEmail(loginRequest.email());
		User user = userRepository.findByEmail(normalizedEmail)
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

		if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
			throw new BadCredentialsException("Invalid email or password.");
		}

		return new LoginResponse(
				jwtService.generateToken(user),
				"Bearer",
				new AuthenticatedUserResponse(user.getId(), user.getName(), user.getEmail()));
	}

	private String normalizeEmail(String email) {
		return email.strip().toLowerCase(Locale.ROOT);
	}
}
