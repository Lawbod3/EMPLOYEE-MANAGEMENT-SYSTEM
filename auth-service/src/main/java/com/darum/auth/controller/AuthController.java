package com.darum.auth.controller;

import com.darum.auth.service.AuthService;
import com.darum.shared.dto.request.AuthRequest;
import com.darum.shared.dto.request.RegisterRequest;
import com.darum.shared.dto.response.ApiResponse;
import com.darum.shared.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

   @PostMapping("/register")
    public ResponseEntity<?> register(@Valid RegisterRequest registerRequest) {
       AuthResponse response = authService.register(registerRequest);
       return new ResponseEntity<>(new ApiResponse(true, response), HttpStatus.CREATED);
   }

   @PostMapping("/login")
    public ResponseEntity<?> login (@Valid AuthRequest authRequest) {
       AuthResponse response = authService.login(authRequest);
       return new ResponseEntity<>(new ApiResponse(true, response), HttpStatus.OK);
   }




}
