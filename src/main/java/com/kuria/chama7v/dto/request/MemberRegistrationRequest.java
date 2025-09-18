package com.kuria.chama7v.dto.request;

import com.kuria.chama7v.entity.enums.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberRegistrationRequest {
    @NotBlank(message = "National ID is required")
    @Size(min = 8, max = 10, message = "National ID must be between 8 and 10 characters")
    private String nationalId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[17][0-9]{8}$", message = "Invalid phone number format. Use 254XXXXXXXXX")
    private String phone;

    // optional: allow admin to set a role when creating - defaults to MEMBER
    private MemberRole role;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}