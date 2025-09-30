package com.kuria.chama7v.controller;

import com.kuria.chama7v.dto.response.ApiResponse;
import com.kuria.chama7v.dto.response.DashboardResponse;
import com.kuria.chama7v.service.DashboardService;
import com.kuria.chama7v.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        var currentMember = memberService.getCurrentMember();
        DashboardResponse dashboard = dashboardService.getMemberDashboard(currentMember.getId());
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", dashboard));
    }

    @GetMapping("/group-stats")
    public ResponseEntity<ApiResponse<DashboardResponse.GroupStats>> getGroupStats() {
        DashboardResponse.GroupStats groupStats = dashboardService.getGroupStats();
        return ResponseEntity.ok(ApiResponse.success("Group statistics retrieved successfully", groupStats));
    }
}
