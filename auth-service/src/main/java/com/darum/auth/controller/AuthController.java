package com.darum.auth.controller;

import com.darum.shared.dto.response.UserResponse;
import com.darum.auth.model.CustomUserDetails;
import com.darum.auth.model.User;
import com.darum.auth.repositories.UserRepository;
import com.darum.auth.service.AuthService;
import com.darum.auth.dto.request.AuthRequest;
import com.darum.auth.dto.request.RegisterRequest;
import com.darum.shared.dto.response.ApiResponse;
import com.darum.auth.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

   @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
       AuthResponse response = authService.register(registerRequest);
       return new ResponseEntity<>(new ApiResponse(true, response), HttpStatus.CREATED);
   }

   @PostMapping("/login")
    public ResponseEntity<?> login (@Valid @RequestBody AuthRequest authRequest) {
       AuthResponse response = authService.login(authRequest);
       return new ResponseEntity<>(new ApiResponse(true, response), HttpStatus.OK);
   }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getById(@AuthenticationPrincipal CustomUserDetails userDetail) {
        Optional<User> userFound = userRepository.findByEmail(userDetail.getUsername());
        return userFound
                .map(user -> ResponseEntity.ok(modelMapper.map(user, UserResponse.class)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/email")
    public ResponseEntity<UserResponse> getByEmail(@RequestParam String email) {
       Optional<User> userFound = userRepository.findByEmail(email);
       return userFound
               .map(user -> ResponseEntity.ok(modelMapper.map(user, UserResponse.class)))
               .orElseGet(() -> ResponseEntity.notFound().build());
    }


}
