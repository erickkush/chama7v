package com.kuria.chama7v.controller;

import com.kuria.chama7v.dto.request.ContributionRequest;
import com.kuria.chama7v.dto.response.ApiResponse;
import com.kuria.chama7v.dto.response.ContributionResponse;
import com.kuria.chama7v.service.ContributionService;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/contributions")
@RequiredArgsConstructor
public class ContributionController {

    private final ContributionService contributionService;
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContributionResponse>> makeContribution(
            @Valid @RequestBody ContributionRequest request) {
        ContributionResponse contribution = contributionService.makeContribution(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contribution made successfully", contribution));
    }

    @GetMapping("/my-contributions")
    public ResponseEntity<ApiResponse<Page<ContributionResponse>>> getMyContributions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var currentMember = memberService.getCurrentMember();
        Pageable pageable = PageRequest.of(page, size, Sort.by("contributionDate").descending());

        Page<ContributionResponse> contributions = contributionService
                .getMemberContributions(currentMember.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Contributions retrieved successfully", contributions));
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Page<ContributionResponse>>> getMemberContributions(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("contributionDate").descending());
        Page<ContributionResponse> contributions = contributionService
                .getMemberContributions(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Member contributions retrieved successfully", contributions));
    }

    @GetMapping
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<Page<ContributionResponse>>> getAllContributions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("contributionDate").descending());

        Page<ContributionResponse> contributions;
        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            contributions = contributionService.getContributionsByDateRange(start, end, pageable);
        } else {
            contributions = contributionService.getAllContributions(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Contributions retrieved successfully", contributions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContributionResponse>> getContributionById(@PathVariable Long id) {
        ContributionResponse contribution = contributionService.getContributionById(id);
        return ResponseEntity.ok(ApiResponse.success("Contribution retrieved successfully", contribution));
    }
}