package com.darum.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
@AllArgsConstructor
public class AuthRequest {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Username is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public AuthRequest() {

    }
}
