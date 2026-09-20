package com.taskflow.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigTest {

	@Test
	void passwordEncoderProducesVerifiableBcryptHashes() {
		PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();
		String rawPassword = "test-password-123";
		String encodedPassword = passwordEncoder.encode(rawPassword);

		assertNotEquals(rawPassword, encodedPassword);
		assertTrue(encodedPassword.startsWith("$2"));
		assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
	}
}
