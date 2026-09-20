package com.taskflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.TaskPageResponse;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.UpdateTaskRequest;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Authenticated task management")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

	private final TaskService taskService;

	@PostMapping
	@Operation(summary = "Create a task")
	public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
	}

	@GetMapping
	@Operation(summary = "List the authenticated user's tasks")
	public TaskPageResponse getTasks(
			@RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must not be negative.") int page,
			@RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be at least 1.") @Max(value = 100, message = "Size must not exceed 100.") int size,
			@RequestParam(required = false) TaskStatus status,
			@RequestParam(required = false) TaskPriority priority,
			@RequestParam(required = false) @Size(max = 100, message = "Search must not exceed 100 characters.") String search) {
		return taskService.getTasks(page, size, status, priority, search);
	}

	@GetMapping("/{taskId}")
	@Operation(summary = "Get one of the authenticated user's tasks")
	public TaskResponse getTask(@PathVariable Long taskId) {
		return taskService.getTask(taskId);
	}

	@PutMapping("/{taskId}")
	@Operation(summary = "Update one of the authenticated user's tasks")
	public TaskResponse updateTask(@PathVariable Long taskId, @Valid @RequestBody UpdateTaskRequest request) {
		return taskService.updateTask(taskId, request);
	}

	@DeleteMapping("/{taskId}")
	@Operation(summary = "Delete one of the authenticated user's tasks")
	public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
		taskService.deleteTask(taskId);
		return ResponseEntity.noContent().build();
	}
}
