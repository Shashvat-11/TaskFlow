package com.taskflow.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void clearUsers() {
		userRepository.deleteAll();
	}

	@Test
	void registerCreatesUserWithBcryptPasswordAndReturnsSafeResponse() throws Exception {
		String rawPassword = "test-password-123";

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "Ada Lovelace",
						  "email": "ADA@EXAMPLE.COM",
						  "password": "%s"
						}
						""".formatted(rawPassword)))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("Ada Lovelace"))
				.andExpect(jsonPath("$.email").value("ada@example.com"))
				.andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.password").doesNotExist());

		User savedUser = userRepository.findByEmail("ada@example.com").orElseThrow();
		assertNotEquals(rawPassword, savedUser.getPassword());
		assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
	}

	@Test
	void registerRejectsDuplicateEmail() throws Exception {
		String registrationRequest = """
				{
				  "name": "Ada Lovelace",
				  "email": "ada@example.com",
				  "password": "test-password-123"
				}
				""";

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registrationRequest))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registrationRequest))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.fieldErrors.email").value("Email is already registered."));
	}

	@Test
	void registerRejectsInvalidRequestData() throws Exception {
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": " ",
						  "email": "not-an-email",
						  "password": "short"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors.name").exists())
				.andExpect(jsonPath("$.fieldErrors.email").exists())
				.andExpect(jsonPath("$.fieldErrors.password").exists());
	}

	@Test
	void loginReturnsJwtForCorrectCredentials() throws Exception {
		String registrationRequest = """
				{
				  "name": "Grace Hopper",
				  "email": "grace@example.com",
				  "password": "test-password-123"
				}
				""";

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registrationRequest))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "GRACE@EXAMPLE.COM",
						  "password": "test-password-123"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.email").value("grace@example.com"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void loginRejectsIncorrectPassword() throws Exception {
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "Grace Hopper",
						  "email": "grace@example.com",
						  "password": "test-password-123"
						}
						"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "grace@example.com",
						  "password": "incorrect-password"
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}
}
