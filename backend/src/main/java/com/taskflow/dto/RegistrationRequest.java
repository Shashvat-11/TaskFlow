package com.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
		@NotBlank(message = "Name must not be blank.")
		@Size(max = 100, message = "Name must not exceed 100 characters.")
		String name,
		@NotBlank(message = "Email must not be blank.")
		@Email(message = "Email must be valid.")
		@Size(max = 255, message = "Email must not exceed 255 characters.")
		String email,
		@NotBlank(message = "Password must not be blank.")
		@Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
		String password) {
}
