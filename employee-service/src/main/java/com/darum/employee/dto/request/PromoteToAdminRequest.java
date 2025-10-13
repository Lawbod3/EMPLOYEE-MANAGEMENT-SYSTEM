package com.darum.employee.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromoteToAdminRequest {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Employee email is required")
    private String email;


}
