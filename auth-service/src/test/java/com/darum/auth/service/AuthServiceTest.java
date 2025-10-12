package com.darum.auth.service;

import com.darum.auth.repositories.UserRepository;
import com.darum.auth.dto.request.AuthRequest;
import com.darum.auth.dto.request.RegisterRequest;

import com.darum.auth.dto.response.AuthResponse;
import com.darum.shared.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.Assert.*;

@SpringBootTest
public class AuthServiceTest {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;

    private RegisterRequest registerRequest;

    private AuthResponse authResponse;

    private AuthRequest authRequest;


    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        registerRequest = new RegisterRequest();
        registerRequest.setPassword("password");
        registerRequest.setEmail("email@email.com");
        registerRequest.setFirstName("firstName");
        registerRequest.setLastName("lastName");

        authRequest = new AuthRequest();
        authRequest.setEmail("email@email.com");
        authRequest.setPassword("password");
    }

    @Test
    public void testThatUserCanRegister(){
      authResponse =  authService.register(registerRequest);
      assertNotNull(authResponse);
      assertEquals(authResponse.getEmail(), registerRequest.getEmail());
      assertTrue(authResponse.getRole().contains("USER"));
    }

    @Test
    public void testThatUserCantRegisterWithSameEmailTwice(){
        authResponse =  authService.register(registerRequest);
        assertNotNull(authResponse);
        assertThrows(UserAlreadyExistsException.class, () -> {authService.register(registerRequest);});
    }

    @Test
    public void testThatUserCanLogin(){
        authResponse =  authService.register(registerRequest);
        assertNotNull(authResponse);
        String registerToken = authResponse.getAccessToken();
        try {
            Thread.sleep(1000); // Wait 1 second because if it generate token without waiting it will be the same
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        authResponse =  authService.login(authRequest);
        assertNotNull(authResponse);
        assertNotEquals(authResponse.getAccessToken(), registerToken);

    }

    @Test
    public void testThatServiceThrowBadCredentialsException(){
        authResponse =  authService.register(registerRequest);
        assertNotNull(authResponse);
        AuthRequest badRequest = new AuthRequest();
        badRequest.setEmail("email@email.com");
        badRequest.setPassword("password1");
        assertThrows(BadCredentialsException.class, () -> {authService.login(badRequest);});
    }




}
