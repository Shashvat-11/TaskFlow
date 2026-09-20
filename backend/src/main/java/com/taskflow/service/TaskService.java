package com.taskflow.service;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.TaskPageResponse;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.UpdateTaskRequest;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import com.taskflow.exception.OwnershipViolationException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

	private final TaskRepository taskRepository;
	private final UserRepository userRepository;

	@Transactional
	public TaskResponse createTask(CreateTaskRequest request) {
		User user = currentUser();
		Task task = new Task();
		applyRequest(task, request.title(), request.description(), request.status(), request.priority(), request.dueDate());
		task.setUser(user);
		return toResponse(taskRepository.save(task));
	}

	@Transactional(readOnly = true)
	public TaskPageResponse getTasks(
			int page,
			int size,
			TaskStatus status,
			TaskPriority priority,
			String search) {
		User user = currentUser();
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Specification<Task> specification = (root, query, builder) -> builder.equal(root.get("user"), user);

		if (status != null) {
			specification = specification.and((root, query, builder) -> builder.equal(root.get("status"), status));
		}
		if (priority != null) {
			specification = specification.and((root, query, builder) -> builder.equal(root.get("priority"), priority));
		}
		if (search != null && !search.isBlank()) {
			String searchTerm = "%" + search.strip().toLowerCase(Locale.ROOT) + "%";
			specification = specification.and((root, query, builder) -> builder.or(
					builder.like(builder.lower(root.get("title")), searchTerm),
					builder.like(builder.lower(root.get("description")), searchTerm)));
		}

		Page<Task> tasks = taskRepository.findAll(specification, pageable);
		return new TaskPageResponse(
				tasks.map(this::toResponse).getContent(),
				tasks.getNumber(),
				tasks.getSize(),
				tasks.getTotalElements(),
				tasks.getTotalPages());
	}

	@Transactional(readOnly = true)
	public TaskResponse getTask(Long taskId) {
		return toResponse(ownedTask(taskId, currentUser()));
	}

	@Transactional
	public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
		Task task = ownedTask(taskId, currentUser());
		applyRequest(task, request.title(), request.description(), request.status(), request.priority(), request.dueDate());
		return toResponse(taskRepository.save(task));
	}

	@Transactional
	public void deleteTask(Long taskId) {
		taskRepository.delete(ownedTask(taskId, currentUser()));
	}

	private User currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new OwnershipViolationException();
		}
		return userRepository.findByEmail(authentication.getName())
				.orElseThrow(() -> new OwnershipViolationException());
	}

	private Task ownedTask(Long taskId, User user) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found."));
		if (!task.getUser().getId().equals(user.getId())) {
			throw new OwnershipViolationException();
		}
		return task;
	}

	private void applyRequest(
			Task task,
			String title,
			String description,
			TaskStatus status,
			TaskPriority priority,
			java.time.LocalDate dueDate) {
		task.setTitle(title.strip());
		task.setDescription(description == null || description.isBlank() ? null : description.strip());
		task.setStatus(status);
		task.setPriority(priority);
		task.setDueDate(dueDate);
	}

	private TaskResponse toResponse(Task task) {
		return new TaskResponse(
				task.getId(),
				task.getTitle(),
				task.getDescription(),
				task.getStatus(),
				task.getPriority(),
				task.getDueDate(),
				task.getCreatedAt(),
				task.getUpdatedAt());
	}
}
