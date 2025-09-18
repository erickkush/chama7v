package com.kuria.chama7v.entity;

import com.kuria.chama7v.entity.enums.MemberRole;
import com.kuria.chama7v.entity.enums.MemberStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String memberNumber;

    @Column(nullable = false)
    private String nationalId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "total_contributions", precision = 19, scale = 2)
    private BigDecimal totalContributions = BigDecimal.ZERO;

    @Column(name = "outstanding_loan", precision = 19, scale = 2)
    private BigDecimal outstandingLoan = BigDecimal.ZERO;

    // Auto-generate memberNumber before saving
    @PrePersist
    public void generateMemberNumber() {
        if (this.memberNumber == null || this.memberNumber.isBlank()) {
            this.memberNumber = "MBR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    // force user to change password after their account was created
    @Column(nullable = false)
    private boolean forcePasswordChange = false;

    @CreationTimestamp
    @Column(name = "date_joined")
    private LocalDateTime dateJoined;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Contribution> contributions;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Loan> loans;
}
