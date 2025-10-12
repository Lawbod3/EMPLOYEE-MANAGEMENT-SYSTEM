package com.darum.auth.controller;

import com.darum.auth.model.User;
import com.darum.auth.repositories.UserRepository;
import com.darum.auth.dto.request.AuthRequest;
import com.darum.auth.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper mapper ;
    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        setupTestUser();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }


    private void setupTestUser() {
        userRepository.deleteAll();
        User user = new User();
        user.setEmail("email@email1.com");
        user.setPassword(passwordEncoder.encode("password")); // Properly encoded
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(List.of("USER"));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }


    @Test
    void testRegisterController() throws Exception {
         registerRequest = new RegisterRequest();
        registerRequest.setEmail("email@email.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("firstName");
        registerRequest.setLastName("lastName");
        String json = mapper.writeValueAsString(registerRequest);
        String uri = "/api/auth/register";
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void testLoginController() throws Exception {
        authRequest = new AuthRequest();
        authRequest.setEmail("email@email1.com");
        authRequest.setPassword("password");
        String json = mapper.writeValueAsString(authRequest);
        String uri = "/api/auth/login";
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value("email@email1.com"))
                .andDo(MockMvcResultHandlers.print());
    }
}
