package com.taskflow.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(fieldError -> fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));

		return buildError(HttpStatus.BAD_REQUEST, "Validation failed.", request, fieldErrors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraintViolationException(
			ConstraintViolationException exception,
			HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "Validation failed.", request, Map.of());
	}

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiError> handleDuplicateEmailException(
			DuplicateEmailException exception,
			HttpServletRequest request) {
		return buildError(
				HttpStatus.CONFLICT,
				exception.getMessage(),
				request,
				Map.of("email", "Email is already registered."));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentialsException(
			BadCredentialsException exception,
			HttpServletRequest request) {
		return buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password.", request, Map.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolationException(HttpServletRequest request) {
		return buildError(HttpStatus.CONFLICT, "The request conflicts with existing data.", request, Map.of());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleResourceNotFoundException(
			ResourceNotFoundException exception,
			HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(OwnershipViolationException.class)
	public ResponseEntity<ApiError> handleOwnershipViolationException(
			OwnershipViolationException exception,
			HttpServletRequest request) {
		return buildError(HttpStatus.FORBIDDEN, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleMalformedRequest(
			HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "Malformed JSON request.", request, Map.of());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "Request contains an invalid value.", request, Map.of());
	}

	private ResponseEntity<ApiError> buildError(
			HttpStatus status,
			String message,
			HttpServletRequest request,
			Map<String, String> fieldErrors) {
		ApiError apiError = new ApiError(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fieldErrors);

		return ResponseEntity.status(status).body(apiError);
	}
}
