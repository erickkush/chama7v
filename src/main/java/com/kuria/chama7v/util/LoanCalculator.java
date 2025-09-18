package com.kuria.chama7v.util;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class LoanCalculator {

    public LoanCalculation calculateLoan(BigDecimal principal, BigDecimal annualInterestRate, int durationMonths) {
        // Convert annual interest rate to monthly
        BigDecimal monthlyInterestRate = annualInterestRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);

        // Calculate monthly payment using amortization formula
        // M = P * [r(1+r)^n] / [(1+r)^n - 1]
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyInterestRate);
        BigDecimal onePlusRPowerN = onePlusR.pow(durationMonths, MathContext.DECIMAL128);

        BigDecimal numerator = principal.multiply(monthlyInterestRate).multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);

        BigDecimal monthlyPayment;
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            // If interest rate is 0, simple division
            monthlyPayment = principal.divide(BigDecimal.valueOf(durationMonths), 2, RoundingMode.HALF_UP);
        } else {
            monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = monthlyPayment.multiply(BigDecimal.valueOf(durationMonths));
        BigDecimal totalInterest = totalAmount.subtract(principal);

        return new LoanCalculation(monthlyPayment, totalAmount, totalInterest);
    }

    @Data
    public static class LoanCalculation {
        private final BigDecimal monthlyPayment;
        private final BigDecimal totalAmount;
        private final BigDecimal totalInterest;
    }
}