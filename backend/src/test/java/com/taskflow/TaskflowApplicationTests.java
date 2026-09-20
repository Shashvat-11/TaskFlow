package com.taskflow;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

class TaskflowApplicationTests {

	@Test
	void applicationIsConfiguredAsSpringBootApplication() {
		assertTrue(TaskflowApplication.class.isAnnotationPresent(SpringBootApplication.class));
	}

}
