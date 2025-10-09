package com.darum.shared.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Setter @Getter
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String email;
    private List<String> role;
    private String employeeId;


    public AuthResponse(String token, String email, List<String> roles) {
        this.accessToken = token;
        this.email = email;
        this.role = roles;
    }
}
