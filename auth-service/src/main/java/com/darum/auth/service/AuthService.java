package com.darum.auth.service;

import com.darum.auth.model.CustomUserDetails;
import com.darum.auth.model.User;
import com.darum.auth.repositories.UserRepository;
import com.darum.auth.dto.request.AuthRequest;
import com.darum.auth.dto.request.RegisterRequest;
import com.darum.auth.dto.response.AuthResponse;
import com.darum.shared.dto.Roles;
import com.darum.shared.dto.request.AddRoleRequest;
import com.darum.shared.dto.response.UserResponse;
import com.darum.shared.exceptions.RoleException;
import com.darum.shared.exceptions.UnauthorizedException;
import com.darum.shared.exceptions.UserAlreadyExistsException;
import com.darum.shared.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());

        // Encode password
        savedUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        savedUser = userRepository.save(savedUser);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        List<String> roles = savedUser.getRoles();
        // Generate token for immediate login after registration
        String token = jwtTokenService.generateToken(savedUser.getEmail(), savedUser.getId(), roles);

        return new AuthResponse(token, savedUser.getEmail(), roles);

    }

    public AuthResponse login(AuthRequest request){
        log.info("Attempting login for email: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        // Cast to CustomUserDetails and get ID
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // extract the email
        String authenticatedEmail = authentication.getName();
        Long userId = userDetails.getId();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtTokenService.generateToken(authenticatedEmail, userId,   roles);
        log.info("Login successful for email: {}", request.getEmail());
        return new AuthResponse(token, authenticatedEmail, roles);
    }

    public UserResponse addRoleToUser(Long userId, String role)  {
        try {
            // Find the user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        return new UserNotFoundException("User not found with ID: " + userId);
                    });
            // ✅ USE ROLES CONSTANT
            if (role.equals(Roles.SUPERADMIN)) {
                throw new UnauthorizedException("Access denied");
            }
            // Get current roles
            List<String> currentRoles = new ArrayList<>(user.getRoles()); // Create new mutable list

            System.out.println("🔍 Current roles list: " + currentRoles);
            // Check if role already exists
            if (currentRoles.contains(role)) {
                throw new RoleException("User already has role: " + role);
            }
            // Add the new role
            currentRoles.add(role);
            user.setRoles(currentRoles);
            user.setUpdatedAt(LocalDateTime.now());

            // Save the updated user
            User updatedUser = userRepository.save(user);

            UserResponse response = modelMapper.map(updatedUser, UserResponse.class);


            return response;
        } catch (RoleException | UnauthorizedException | UserNotFoundException e) {
            // Re-throw specific exceptions
            throw e;
        } catch (Exception e) {

            throw new RuntimeException("Failed to add role: " + e.getMessage(), e);
        }
    }

}
