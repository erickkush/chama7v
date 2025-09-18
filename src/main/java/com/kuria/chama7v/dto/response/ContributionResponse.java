package com.kuria.chama7v.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ContributionResponse {
    private Long id;
    private String memberName;
    private String memberNumber;
    private BigDecimal amount;
    private String transactionReference;
    private String mpesaReceiptNumber;
    private String description;
    private LocalDateTime contributionDate;
}