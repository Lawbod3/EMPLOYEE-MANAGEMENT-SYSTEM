package com.darum.auth.repositories;

import com.darum.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        // If you want to create a test user, do it here:
        // User user = new User();
        // user.setUsername("test");
        // user.setPassword("password");
        // userRepository.save(user);
    }

    @Test
    public void testRepositoryIsEmpty() {
        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    public void testRepositoryContainsUser() {
        User user = new User();
        user.setEmail("email@email.com");
        user.setPassword("password");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setRoles(List.of("USER"));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(true);
        userRepository.save(user);
        assertTrue(userRepository.findAll().contains(user));
    }
}