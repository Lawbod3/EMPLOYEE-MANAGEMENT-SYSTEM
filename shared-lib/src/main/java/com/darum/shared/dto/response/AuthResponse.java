package com.darum.shared.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter @Getter
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private String email;
    private List<String> role;
    private String employeeId;
}
