package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.dto.response.ReportResponse;
import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.exception.ResourceNotFoundException;
import com.kuria.chama7v.repository.ContributionRepository;
import com.kuria.chama7v.repository.LoanRepository;
import com.kuria.chama7v.repository.MemberRepository;
import com.kuria.chama7v.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;

    @Override
    public ReportResponse getMemberContributionReport(Long memberId, LocalDateTime startDate,
                                                      LocalDateTime endDate, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        ReportResponse response = new ReportResponse();
        response.setReportType("MEMBER_CONTRIBUTIONS");
        response.setPeriod(startDate + " to " + endDate);

        // Get contributions data
        var contributions = contributionRepository.findByMemberOrderByContributionDateDesc(member, pageable);
        response.setData(contributions.getContent().stream().map(c -> (Object) c).toList());
        response.setRecordCount((long) contributions.getContent().size());

        // Calculate totals
        BigDecimal totalAmount = contributionRepository.getTotalContributionsByMember(member);
        response.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);

        // Summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGroupContributions", totalAmount);
        summary.put("totalMembers", memberRepository.getActiveMemberCount());
        summary.put("averageContributionPerMember",
                memberRepository.getActiveMemberCount() > 0 ?
                        totalAmount.divide(BigDecimal.valueOf(memberRepository.getActiveMemberCount()), 2, BigDecimal.ROUND_HALF_UP) :
                        BigDecimal.ZERO);

        response.setSummary(summary);

        return response;
    }

    @Override
    public ReportResponse getGroupContributionReport(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        ReportResponse response = new ReportResponse();
        response.setReportType("GROUP_CONTRIBUTIONS");
        response.setPeriod(startDate + " to " + endDate);

        // Get contributions in date range
        var contributions = contributionRepository.findByDateRange(startDate, endDate, pageable);
        response.setData(contributions.getContent().stream().map(c -> (Object) c).toList());
        response.setRecordCount(contributions.getTotalElements());

        // Calculate totals
        BigDecimal totalGroupContributions = contributionRepository.getTotalContributions();
        response.setTotalAmount(totalGroupContributions != null ? totalGroupContributions : BigDecimal.ZERO);

        // Summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGroupContributions", totalGroupContributions);
        summary.put("totalMembers", memberRepository.getActiveMemberCount());
        summary.put("averageContributionPerMember",
                memberRepository.getActiveMemberCount() > 0 ?
                        totalGroupContributions.divide(
                                BigDecimal.valueOf(memberRepository.getActiveMemberCount()),
                                2,
                                BigDecimal.ROUND_HALF_UP
                        ) : BigDecimal.ZERO);

        response.setSummary(summary);

        return response;
    }


    @Override
    public ReportResponse getLoanReport(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        ReportResponse response = new ReportResponse();
        response.setReportType("LOAN_REPORT");
        response.setPeriod(startDate + " to " + endDate);

        // Get loans data
        var loans = loanRepository.findByDateRange(startDate, endDate, pageable);
        response.setData(loans.getContent().stream().map(l -> (Object) l).toList());
        response.setRecordCount(loans.getTotalElements());

        // Calculate totals
        BigDecimal totalDisbursed = loanRepository.getTotalDisbursedLoans();
        BigDecimal totalOutstanding = loanRepository.getTotalOutstandingLoans();
        response.setTotalAmount(totalDisbursed != null ? totalDisbursed : BigDecimal.ZERO);

        // Summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDisbursedLoans", totalDisbursed);
        summary.put("totalOutstandingLoans", totalOutstanding);
        summary.put("pendingApplications", loanRepository.getPendingLoanCount());

        response.setSummary(summary);

        return response;
    }

    @Override
    public ReportResponse getMemberLoanReport(Long memberId, LocalDateTime startDate,
                                              LocalDateTime endDate, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        ReportResponse response = new ReportResponse();
        response.setReportType("MEMBER_LOANS");
        response.setPeriod(startDate + " to " + endDate);

        // Get member's loans
        var loans = loanRepository.findByMemberOrderByApplicationDateDesc(member, pageable);
        response.setData(loans.getContent().stream().map(l -> (Object) l).toList());
        response.setRecordCount((long) loans.getContent().size());

        // Calculate member's loan totals
        BigDecimal totalBorrowed = loans.getContent().stream()
                .map(loan -> loan.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalAmount(totalBorrowed);

        // Summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("memberName", member.getName());
        summary.put("memberNumber", member.getMemberNumber());
        summary.put("totalBorrowed", totalBorrowed);
        summary.put("outstandingLoan", member.getOutstandingLoan());
        summary.put("totalLoans", loans.getContent().size());

        response.setSummary(summary);

        return response;
    }
}