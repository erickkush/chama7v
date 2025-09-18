package com.kuria.chama7v.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonIgnore
    private Loan loan;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "principal_amount", precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 19, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "mpesa_receipt_number")
    private String mpesaReceiptNumber;

    @CreationTimestamp
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
}