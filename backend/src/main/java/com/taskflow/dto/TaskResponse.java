package com.taskflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

public record TaskResponse(
		Long id,
		String title,
		String description,
		TaskStatus status,
		TaskPriority priority,
		LocalDate dueDate,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
}
