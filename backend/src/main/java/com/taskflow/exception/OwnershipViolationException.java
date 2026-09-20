package com.taskflow.exception;

public class OwnershipViolationException extends RuntimeException {

	public OwnershipViolationException() {
		super("You do not have permission to access this task.");
	}
}
