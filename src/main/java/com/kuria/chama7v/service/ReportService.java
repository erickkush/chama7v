package com.kuria.chama7v.service;

import com.kuria.chama7v.dto.response.ReportResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ReportService {
    ReportResponse getMemberContributionReport(Long memberId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    ReportResponse getGroupContributionReport(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    ReportResponse getLoanReport(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    ReportResponse getMemberLoanReport(Long memberId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}