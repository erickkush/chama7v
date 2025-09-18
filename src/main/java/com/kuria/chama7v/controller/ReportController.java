package com.kuria.chama7v.controller;

import com.kuria.chama7v.dto.response.ApiResponse;
import com.kuria.chama7v.dto.response.ReportResponse;
import com.kuria.chama7v.service.MemberService;
import com.kuria.chama7v.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final MemberService memberService;

    @GetMapping("/member/contributions")
    public ResponseEntity<ApiResponse<ReportResponse>> getMemberContributionReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        var currentMember = memberService.getCurrentMember();
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        Pageable pageable = PageRequest.of(page, size);

        ReportResponse report = reportService.getMemberContributionReport(
                currentMember.getId(), start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success("Report generated successfully", report));
    }

    @GetMapping("/member/loans")
    public ResponseEntity<ApiResponse<ReportResponse>> getMemberLoanReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        var currentMember = memberService.getCurrentMember();
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        Pageable pageable = PageRequest.of(page, size);

        ReportResponse report = reportService.getMemberLoanReport(
                currentMember.getId(), start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success("Report generated successfully", report));
    }

    @GetMapping("/admin/group-contributions")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<ReportResponse>> getGroupContributionReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        Pageable pageable = PageRequest.of(page, size);

        ReportResponse report = reportService.getGroupContributionReport(start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success("Report generated successfully", report));
    }

    @GetMapping("/admin/loans")
    @PreAuthorize("hasRole('CHAIRPERSON') or hasRole('SECRETARY') or hasRole('TREASURER')")
    public ResponseEntity<ApiResponse<ReportResponse>> getLoanReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        Pageable pageable = PageRequest.of(page, size);

        ReportResponse report = reportService.getLoanReport(start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success("Report generated successfully", report));
    }
}