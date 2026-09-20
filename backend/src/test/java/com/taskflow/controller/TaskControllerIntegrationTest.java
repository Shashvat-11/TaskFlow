package com.taskflow.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void clearDatabase() {
		taskRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void authenticatedUserCanCreateListRetrieveUpdateAndDeleteOwnTask() throws Exception {
		String token = authenticate("Ada Lovelace", "ada@example.com");
		long taskId = createTask(token, "Prepare demo", "Draft the presentation", "TODO", "MEDIUM", "2026-10-01");

		mockMvc.perform(get("/api/tasks").header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(taskId))
				.andExpect(jsonPath("$.totalElements").value(1));

		mockMvc.perform(get("/api/tasks/{taskId}", taskId).header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Prepare demo"));

		mockMvc.perform(put("/api/tasks/{taskId}", taskId)
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(taskRequest("Present demo", "Final slides", "COMPLETED", "HIGH", "2026-10-02")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Present demo"))
				.andExpect(jsonPath("$.status").value("COMPLETED"));

		mockMvc.perform(delete("/api/tasks/{taskId}", taskId).header("Authorization", bearer(token)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tasks/{taskId}", taskId).header("Authorization", bearer(token)))
				.andExpect(status().isNotFound());
	}

	@Test
	void userCannotReadModifyOrDeleteAnotherUsersTask() throws Exception {
		String ownerToken = authenticate("Ada Lovelace", "ada@example.com");
		String otherToken = authenticate("Grace Hopper", "grace@example.com");
		long taskId = createTask(ownerToken, "Private task", "Only Ada can access this", "TODO", "LOW", null);

		mockMvc.perform(get("/api/tasks/{taskId}", taskId).header("Authorization", bearer(otherToken)))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/api/tasks/{taskId}", taskId)
				.header("Authorization", bearer(otherToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(taskRequest("Hijacked task", "Nope", "COMPLETED", "HIGH", null)))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/tasks/{taskId}", taskId).header("Authorization", bearer(otherToken)))
				.andExpect(status().isForbidden());
	}

	@Test
	void taskListSupportsStatusPrioritySearchAndPagination() throws Exception {
		String token = authenticate("Ada Lovelace", "ada@example.com");
		createTask(token, "Roadmap presentation", "Quarterly planning", "TODO", "HIGH", null);
		createTask(token, "Write meeting notes", "Team meeting follow-up", "TODO", "LOW", null);
		createTask(token, "Close retrospective", "Archive the sprint", "COMPLETED", "HIGH", null);

		mockMvc.perform(get("/api/tasks")
				.header("Authorization", bearer(token))
				.param("status", "TODO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2));

		mockMvc.perform(get("/api/tasks")
				.header("Authorization", bearer(token))
				.param("priority", "HIGH"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2));

		mockMvc.perform(get("/api/tasks")
				.header("Authorization", bearer(token))
				.param("search", "meeting"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].title").value("Write meeting notes"));

		mockMvc.perform(get("/api/tasks")
				.header("Authorization", bearer(token))
				.param("page", "0")
				.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(3));
	}

	@Test
	void invalidTaskIsRejected() throws Exception {
		String token = authenticate("Ada Lovelace", "ada@example.com");

		mockMvc.perform(post("/api/tasks")
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(taskRequest(" ", "Description", "TODO", "LOW", null)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.title").exists());
	}

	private String authenticate(String name, String email) throws Exception {
		String password = "test-password-123";
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "name": "%s",
						  "email": "%s",
						  "password": "%s"
						}
						""".formatted(name, email, password)))
				.andExpect(status().isCreated());

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "email": "%s",
						  "password": "%s"
						}
						""".formatted(email, password)))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
	}

	private long createTask(
			String token,
			String title,
			String description,
			String status,
			String priority,
			String dueDate) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/tasks")
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content(taskRequest(title, description, status, priority, dueDate)))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		return response.get("id").asLong();
	}

	private String taskRequest(String title, String description, String status, String priority, String dueDate) {
		String dueDateField = dueDate == null ? "null" : "\"%s\"".formatted(dueDate);
		return """
				{
				  "title": "%s",
				  "description": "%s",
				  "status": "%s",
				  "priority": "%s",
				  "dueDate": %s
				}
				""".formatted(title, description, status, priority, dueDateField);
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}
}
