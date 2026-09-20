package com.taskflow.dto;

import java.time.LocalDateTime;

public record RegistrationResponse(Long id, String name, String email, LocalDateTime createdAt) {
}
