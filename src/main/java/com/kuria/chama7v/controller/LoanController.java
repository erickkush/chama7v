package com.kuria.chama7v.controller;

import com.kuria.chama7v.dto.request.LoanApplicationRequest;
import com.kuria.chama7v.dto.response.ApiResponse;
import com.kuria.chama7v.dto.response.LoanResponse;
import com.kuria.chama7v.service.LoanService;
import com.kuria.chama7v.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final MemberService memberService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request) {
        LoanResponse loan = loanService.applyForLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan application submitted successfully", loan));
    }

    @GetMapping("/my-loans")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getMyLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var currentMember = memberService.getCurrentMember();
        Pageable pageable = PageRequest.of(page, size, Sort.by("applicationDate").descending());

        Page<LoanResponse> loans = loanService.getMemberLoans(currentMember.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Loans retrieved successfully", loans));
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getMemberLoans(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("applicationDate").descending());
        Page<LoanResponse> loans = loanService.getMemberLoans(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Member loans retrieved successfully", loans));
    }

    @GetMapping
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("applicationDate").descending());
        Page<LoanResponse> loans = loanService.getAllLoans(pageable);
        return ResponseEntity.ok(ApiResponse.success("Loans retrieved successfully", loans));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getPendingLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("applicationDate").ascending());
        Page<LoanResponse> loans = loanService.getPendingLoans(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending loans retrieved successfully", loans));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(@PathVariable Long id) {
        LoanResponse loan = loanService.approveLoan(id);
        return ResponseEntity.ok(ApiResponse.success("Loan approved successfully", loan));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<LoanResponse>> rejectLoan(
            @PathVariable Long id,
            @RequestParam String reason) {
        LoanResponse loan = loanService.rejectLoan(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Loan rejected", loan));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable Long id) {
        LoanResponse loan = loanService.getLoanById(id);
        return ResponseEntity.ok(ApiResponse.success("Loan retrieved successfully", loan));
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<ApiResponse<LoanResponse>> makeLoanPayment(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        LoanResponse loan = loanService.makeLoanPayment(id, amount);
        return ResponseEntity.ok(ApiResponse.success("Loan payment processed successfully", loan));
    }
}