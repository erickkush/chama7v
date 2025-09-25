package com.kuria.chama7v.dto.request;

import com.kuria.chama7v.entity.enums.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberRegistrationRequest {
    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^[0-9]{8}$", message = "Invalid national ID format")
    private String nationalId;

    @NotBlank(message = "Full name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private MemberRole role; // Optional, defaults to MEMBER
}