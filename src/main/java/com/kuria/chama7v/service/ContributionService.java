package com.kuria.chama7v.service;

import com.kuria.chama7v.dto.request.ContributionRequest;
import com.kuria.chama7v.dto.response.ContributionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ContributionService {
    ContributionResponse makeContribution(ContributionRequest request);
    Page<ContributionResponse> getMemberContributions(Long memberId, Pageable pageable);
    Page<ContributionResponse> getAllContributions(Pageable pageable);
    Page<ContributionResponse> getContributionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    ContributionResponse getContributionById(Long id);
}