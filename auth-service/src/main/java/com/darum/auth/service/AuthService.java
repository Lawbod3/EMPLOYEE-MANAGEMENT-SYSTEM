package com.darum.auth.service;

import com.darum.auth.model.User;
import com.darum.auth.repositories.UserRepository;
import com.darum.shared.dto.request.AuthRequest;
import com.darum.shared.dto.request.RegisterRequest;
import com.darum.shared.dto.response.AuthResponse;
import com.darum.shared.exceptions.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    // returning response to automatically login after registration
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        // Create new user
       User savedUser = modelMapper.map(request, User.class);

        // Encode password
        savedUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        savedUser = userRepository.save(savedUser);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        List<String> roles = List.of(savedUser.getRole());
        // Generate token for immediate login after registration
        String token = jwtTokenService.generateToken(savedUser.getEmail(), roles);

        return new AuthResponse(token, savedUser.getEmail(), roles);

    }

    public AuthResponse login(AuthRequest request){
        log.info("Attempting login for email: {}", request.getEmail());


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // extract the email
        String authenticatedEmail = authentication.getName();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtTokenService.generateToken(authenticatedEmail, roles);

        log.info("Login successful for email: {}", request.getEmail());

        return new AuthResponse(token, authenticatedEmail, roles);

    }


}
