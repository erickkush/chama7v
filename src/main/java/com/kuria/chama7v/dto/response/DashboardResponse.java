package com.kuria.chama7v.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardResponse {
    private MemberStats memberStats;
    private GroupStats groupStats;
    private List<ContributionResponse> recentContributions;
    private List<LoanResponse> recentLoans;

    @Data
    public static class MemberStats {
        private BigDecimal totalContributions;
        private BigDecimal outstandingLoan;
        private Long activeLoans;
        private BigDecimal availableLoanLimit;
    }

    @Data
    public static class GroupStats {
        private BigDecimal totalGroupContributions;
        private BigDecimal totalOutstandingLoans;
        private Long totalActiveMembers;
        private Long pendingLoanApplications;
        private BigDecimal totalDisbursedLoans;
    }
}