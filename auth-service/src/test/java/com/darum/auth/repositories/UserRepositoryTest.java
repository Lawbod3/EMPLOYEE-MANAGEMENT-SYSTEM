package com.darum.auth.repositories;

import com.darum.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

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
}