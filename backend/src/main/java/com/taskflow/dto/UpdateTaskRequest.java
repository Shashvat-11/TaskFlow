package com.taskflow.dto;

import java.time.LocalDate;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
		@NotBlank(message = "Title must not be blank.")
		@Size(max = 200, message = "Title must not exceed 200 characters.")
		String title,
		@Size(max = 2000, message = "Description must not exceed 2000 characters.")
		String description,
		@NotNull(message = "Status is required.")
		TaskStatus status,
		@NotNull(message = "Priority is required.")
		TaskPriority priority,
		LocalDate dueDate) {
}
