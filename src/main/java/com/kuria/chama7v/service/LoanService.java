package com.kuria.chama7v.service;


import com.kuria.chama7v.dto.request.LoanApplicationRequest;
import com.kuria.chama7v.dto.response.LoanResponse;
import com.kuria.chama7v.entity.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LoanService {
    LoanResponse applyForLoan(LoanApplicationRequest request);
    Page<LoanResponse> getMemberLoans(Long memberId, Pageable pageable);
    Page<LoanResponse> getAllLoans(Pageable pageable);
    Page<LoanResponse> getPendingLoans(Pageable pageable);
    Page<LoanResponse> filterLoans(Long memberId, LoanStatus status, BigDecimal minAmount, BigDecimal maxAmount,
                                   LocalDateTime from, LocalDateTime to, Pageable pageable);
    LoanResponse approveLoan(Long loanId);
    LoanResponse rejectLoan(Long loanId, String reason);
    LoanResponse getLoanById(Long id);
    LoanResponse makeLoanPayment(Long loanId, java.math.BigDecimal amount);
}