package com.kuria.chama7v.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContributionRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Contribution amount must be at least 1.00")
    private BigDecimal amount;

    private String description;

    private String phoneNumber; // For M-Pesa payment
}