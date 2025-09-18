package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.dto.response.ContributionResponse;
import com.kuria.chama7v.dto.response.DashboardResponse;
import com.kuria.chama7v.dto.response.LoanResponse;
import com.kuria.chama7v.entity.Contribution;
import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.LoanStatus;
import com.kuria.chama7v.exception.ResourceNotFoundException;
import com.kuria.chama7v.repository.ContributionRepository;
import com.kuria.chama7v.repository.LoanRepository;
import com.kuria.chama7v.repository.MemberRepository;
import com.kuria.chama7v.service.ContributionService;
import com.kuria.chama7v.service.DashboardService;
import com.kuria.chama7v.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final ContributionService contributionService;
    private final LoanService loanService;

    @Override
    public DashboardResponse getMemberDashboard(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        DashboardResponse response = new DashboardResponse();

        // Member stats
        DashboardResponse.MemberStats memberStats = new DashboardResponse.MemberStats();
        memberStats.setTotalContributions(member.getTotalContributions());
        memberStats.setOutstandingLoan(member.getOutstandingLoan());

        // Count active loans
        Long activeLoans = (long) loanRepository.findByMemberAndStatusIn(member,
                Arrays.asList(LoanStatus.APPROVED, LoanStatus.DISBURSED)).size();
        memberStats.setActiveLoans(activeLoans);

        // Calculate available loan limit (3x contributions minus outstanding loans)
        BigDecimal availableLoanLimit = member.getTotalContributions()
                .multiply(BigDecimal.valueOf(3))
                .subtract(member.getOutstandingLoan());
        memberStats.setAvailableLoanLimit(availableLoanLimit.max(BigDecimal.ZERO));

        response.setMemberStats(memberStats);

        // Group stats (for management roles)
        response.setGroupStats(getGroupStats());

        // Recent contributions (last 10)
        Pageable recentPage = PageRequest.of(0, 10, Sort.by("contributionDate").descending());
        Page<Contribution> recent = contributionRepository.findByMemberOrderByContributionDateDesc(member, recentPage);
        List<ContributionResponse> recentContributions = recent.getContent().stream()
                .map(c -> {
                    ContributionResponse r = new ContributionResponse();
                    r.setId(c.getId());
                    r.setAmount(c.getAmount());
                    r.setDescription(c.getDescription());
                    r.setContributionDate(c.getContributionDate());
                    r.setTransactionReference(c.getTransactionReference());
                    return r;
                }).toList();
        response.setRecentContributions(recentContributions);


        return response;
    }

    @Override
    public DashboardResponse.GroupStats getGroupStats() {
        DashboardResponse.GroupStats groupStats = new DashboardResponse.GroupStats();

        groupStats.setTotalGroupContributions(
                contributionRepository.getTotalContributions() != null ?
                        contributionRepository.getTotalContributions() : BigDecimal.ZERO);

        groupStats.setTotalOutstandingLoans(
                loanRepository.getTotalOutstandingLoans() != null ?
                        loanRepository.getTotalOutstandingLoans() : BigDecimal.ZERO);

        groupStats.setTotalActiveMembers(memberRepository.getActiveMemberCount());
        groupStats.setPendingLoanApplications(loanRepository.getPendingLoanCount());

        groupStats.setTotalDisbursedLoans(
                loanRepository.getTotalDisbursedLoans() != null ?
                        loanRepository.getTotalDisbursedLoans() : BigDecimal.ZERO);

        return groupStats;
    }
}