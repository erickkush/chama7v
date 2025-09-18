package com.kuria.chama7v.dto.response;

import com.kuria.chama7v.entity.enums.MemberRole;
import com.kuria.chama7v.entity.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String memberNumber;
    private String name;
    private String email;
    private MemberRole role;
    private MemberStatus status;
    private boolean forcePasswordChange;

    public JwtResponse(String token, Long id, String memberNumber, String name, String email, MemberRole role, MemberStatus status) {
        this.token = token;
        this.id = id;
        this.memberNumber = memberNumber;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
    }
}