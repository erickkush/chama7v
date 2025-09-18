package com.kuria.chama7v.dto.response;

import com.kuria.chama7v.entity.enums.LoanStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanResponse {
    private Long id;
    private String loanNumber;
    private String memberName;
    private String memberNumber;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer durationMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private LoanStatus status;
    private String purpose;
    private String approvedBy;
    private LocalDateTime approvalDate;
    private LocalDateTime applicationDate;
}