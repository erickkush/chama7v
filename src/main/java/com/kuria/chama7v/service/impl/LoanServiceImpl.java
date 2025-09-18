package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.dto.request.LoanApplicationRequest;
import com.kuria.chama7v.dto.response.LoanResponse;
import com.kuria.chama7v.entity.Loan;
import com.kuria.chama7v.entity.LoanPayment;
import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.LoanStatus;
import com.kuria.chama7v.exception.ResourceNotFoundException;
import com.kuria.chama7v.repository.LoanPaymentRepository;
import com.kuria.chama7v.repository.LoanRepository;
import com.kuria.chama7v.repository.MemberRepository;
import com.kuria.chama7v.service.EmailService;
import com.kuria.chama7v.service.LoanService;
import com.kuria.chama7v.service.MemberService;
import com.kuria.chama7v.specification.LoanSpecification;
import com.kuria.chama7v.util.LoanCalculator;
import com.kuria.chama7v.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final LoanCalculator loanCalculator;
    private final ValidationUtil validationUtil;
    private final EmailService emailService;

    @Override
    @Transactional
    public LoanResponse applyForLoan(LoanApplicationRequest request) {
        Member member = memberService.getCurrentMember();

        // Check if member has any pending or active loans
        if (hasActiveLoan(member)) {
            throw new IllegalArgumentException("You already have an active or pending loan application");
        }

        // Calculate loan details
        LoanCalculator.LoanCalculation calculation = loanCalculator.calculateLoan(
                request.getAmount(), request.getInterestRate(), request.getDurationMonths());

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setLoanNumber(generateUniqueLoanNumber());
        loan.setAmount(request.getAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setDurationMonths(request.getDurationMonths());
        loan.setMonthlyPayment(calculation.getMonthlyPayment());
        loan.setTotalAmount(calculation.getTotalAmount());
        loan.setBalance(calculation.getTotalAmount());
        loan.setPurpose(request.getPurpose());
        loan.setStatus(LoanStatus.PENDING);

        Loan savedLoan = loanRepository.save(loan);

        log.info("Loan application submitted by member {}: {}", member.getEmail(), request.getAmount());

        return mapToLoanResponse(savedLoan);
    }

    private boolean hasActiveLoan(Member member) {
        return !loanRepository.findByMemberAndStatusIn(member,
                        Arrays.asList(LoanStatus.PENDING, LoanStatus.APPROVED, LoanStatus.DISBURSED))
                .isEmpty();
    }

    private String generateUniqueLoanNumber() {
        String loanNumber;
        do {
            loanNumber = validationUtil.generateLoanNumber();
        } while (loanRepository.findByLoanNumber(loanNumber).isPresent());
        return loanNumber;
    }

    @Override
    public Page<LoanResponse> getMemberLoans(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        return loanRepository.findByMemberOrderByApplicationDateDesc(member, pageable)
                .map(this::mapToLoanResponse);
    }

    @Override
    public Page<LoanResponse> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable)
                .map(this::mapToLoanResponse);
    }
    @Override
    public Page<LoanResponse> filterLoans(Long memberId, LoanStatus status, BigDecimal minAmount, BigDecimal maxAmount,
                                          LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<Loan> spec = LoanSpecification.filter(memberId, status, minAmount, maxAmount, from, to);
        return loanRepository.findAll(spec, pageable).map(this::mapToLoanResponse);
    }

    @Override
    public Page<LoanResponse> getPendingLoans(Pageable pageable) {
        return loanRepository.findByStatusOrderByApplicationDateDesc(LoanStatus.PENDING, pageable)
                .map(this::mapToLoanResponse);
    }

    @Override
    @Transactional
    public LoanResponse approveLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalArgumentException("Only pending loans can be approved");
        }

        Member currentMember = memberService.getCurrentMember();

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedBy(currentMember.getName());
        loan.setApprovalDate(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);

        // Update member's outstanding loan
        Member member = loan.getMember();
        member.setOutstandingLoan(member.getOutstandingLoan().add(loan.getAmount()));
        memberRepository.save(member);

        // Send approval email
        emailService.sendLoanApprovalEmail(member.getEmail(), member.getName(), loan.getLoanNumber());

        log.info("Loan approved: {} by {}", loan.getLoanNumber(), currentMember.getEmail());

        return mapToLoanResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse rejectLoan(Long loanId, String reason) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalArgumentException("Only pending loans can be rejected");
        }

        Member currentMember = memberService.getCurrentMember();

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(reason);

        Loan savedLoan = loanRepository.save(loan);

        // Send rejection email
        Member member = loan.getMember();
        emailService.sendLoanRejectionEmail(member.getEmail(), member.getName(), loan.getLoanNumber(), reason);

        log.info("Loan rejected: {} by {}", loan.getLoanNumber(), currentMember.getEmail());

        return mapToLoanResponse(savedLoan);
    }

    @Override
    public LoanResponse getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));

        return mapToLoanResponse(loan);
    }

    @Override
    @Transactional
    public LoanResponse makeLoanPayment(Long loanId, BigDecimal amount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));

        if (loan.getStatus() != LoanStatus.APPROVED && loan.getStatus() != LoanStatus.DISBURSED) {
            throw new IllegalArgumentException("Can only make payments on approved or disbursed loans");
        }

        if (amount.compareTo(loan.getBalance()) > 0) {
            throw new IllegalArgumentException("Payment amount cannot exceed outstanding balance");
        }

        // Create payment record
        LoanPayment payment = new LoanPayment();
        payment.setLoan(loan);
        payment.setAmount(amount);
        payment.setTransactionReference("PAY-" + System.currentTimeMillis());

        // Calculate principal and interest portions (simplified)
        BigDecimal interestRate = loan.getInterestRate().divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal monthlyInterestRate = interestRate.divide(BigDecimal.valueOf(12), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal interestAmount = loan.getBalance().multiply(monthlyInterestRate);
        BigDecimal principalAmount = amount.subtract(interestAmount);

        if (principalAmount.compareTo(BigDecimal.ZERO) < 0) {
            principalAmount = amount;
            interestAmount = BigDecimal.ZERO;
        }

        payment.setPrincipalAmount(principalAmount);
        payment.setInterestAmount(interestAmount);

        loanPaymentRepository.save(payment);

        // Update loan
        loan.setAmountPaid(loan.getAmountPaid().add(amount));
        loan.setBalance(loan.getBalance().subtract(amount));

        if (loan.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.PAID);
            loan.setBalance(BigDecimal.ZERO);
        }

        Loan savedLoan = loanRepository.save(loan);

        // Update member's outstanding loan
        Member member = loan.getMember();
        member.setOutstandingLoan(member.getOutstandingLoan().subtract(amount));
        memberRepository.save(member);

        log.info("Loan payment made: {} for loan {}", amount, loan.getLoanNumber());

        return mapToLoanResponse(savedLoan);
    }

    private LoanResponse mapToLoanResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setLoanNumber(loan.getLoanNumber());
        response.setMemberName(loan.getMember().getName());
        response.setMemberNumber(loan.getMember().getMemberNumber());
        response.setAmount(loan.getAmount());
        response.setInterestRate(loan.getInterestRate());
        response.setDurationMonths(loan.getDurationMonths());
        response.setMonthlyPayment(loan.getMonthlyPayment());
        response.setTotalAmount(loan.getTotalAmount());
        response.setAmountPaid(loan.getAmountPaid());
        response.setBalance(loan.getBalance());
        response.setStatus(loan.getStatus());
        response.setPurpose(loan.getPurpose());
        response.setApprovedBy(loan.getApprovedBy());
        response.setApprovalDate(loan.getApprovalDate());
        response.setApplicationDate(loan.getApplicationDate());
        return response;
    }
}