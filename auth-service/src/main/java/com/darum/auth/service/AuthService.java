package com.darum.auth.service;

import com.darum.auth.repositories.UserRepository;
import com.darum.shared.dto.request.AuthRequest;
import com.darum.shared.dto.request.RegisterRequest;
import com.darum.shared.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    // returning response to automatically login after registration
    //public AuthResponse register(RegisterRequest authRequest) {
   // }

    public AuthResponse login(AuthRequest request){
        log.info("Attempting login for email: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String authenticatedEmail = authentication.getName();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtTokenService.generateToken(authenticatedEmail, roles);

        log.info("Login successful for email: {}", request.getEmail());

        return new AuthResponse(token, request.getEmail(), roles);

    }


}
