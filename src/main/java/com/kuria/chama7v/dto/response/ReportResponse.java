package com.kuria.chama7v.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ReportResponse {
    private String reportType;
    private String period;
    private BigDecimal totalAmount;
    private Long recordCount;
    private Map<String, Object> summary;
    private List<Object> data;
}