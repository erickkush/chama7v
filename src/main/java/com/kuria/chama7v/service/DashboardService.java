package com.kuria.chama7v.service;

import com.kuria.chama7v.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse getMemberDashboard(Long memberId);
    DashboardResponse.GroupStats getGroupStats();
}