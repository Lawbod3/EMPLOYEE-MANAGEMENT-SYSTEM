package com.darum.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;

@Getter
@Setter
public class DemoteManagerRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

}
