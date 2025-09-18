package com.kuria.chama7v.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MpesaStkRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^254[17][0-9]{8}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    private BigDecimal amount;

    @NotBlank(message = "Account reference is required")
    private String accountReference;

    @NotBlank(message = "Transaction description is required")
    private String transactionDesc;

    private String transactionType; // CONTRIBUTION, LOAN_PAYMENT
}