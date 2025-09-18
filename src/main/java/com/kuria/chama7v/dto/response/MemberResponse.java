package com.kuria.chama7v.dto.response;

import com.kuria.chama7v.entity.enums.MemberRole;
import com.kuria.chama7v.entity.enums.MemberStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberResponse {
    private Long id;
    private String memberNumber;
    private String nationalId;
    private String name;
    private String email;
    private String phone;
    private MemberRole role;
    private MemberStatus status;
    private BigDecimal totalContributions;
    private BigDecimal outstandingLoan;
    private LocalDateTime dateJoined;
}