package com.taskflow.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.taskflow.entity.User;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	void savePopulatesIdentifierAndTimestampsAndFindsUserByEmail() {
		User user = new User();
		user.setName("Ada Lovelace");
		user.setEmail("ada@example.com");
		user.setPassword("stored-password-hash");

		User savedUser = userRepository.saveAndFlush(user);

		assertNotNull(savedUser.getId());
		assertNotNull(savedUser.getCreatedAt());
		assertNotNull(savedUser.getUpdatedAt());
		assertEquals(savedUser.getId(), userRepository.findByEmail("ada@example.com").orElseThrow().getId());
	}

	@Test
	void findByEmailReturnsEmptyWhenUserDoesNotExist() {
		assertFalse(userRepository.findByEmail("missing@example.com").isPresent());
	}
}
