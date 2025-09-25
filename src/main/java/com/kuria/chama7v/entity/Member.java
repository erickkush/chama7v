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

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 4)
    private String memberNumber;

    @Column(nullable = false, unique = true, length = 8)
    private String nationalId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.PENDING;

    @Column(name = "total_contributions", precision = 19, scale = 2)
    private BigDecimal totalContributions = BigDecimal.ZERO;

    @Column(name = "outstanding_loan", precision = 19, scale = 2)
    private BigDecimal outstandingLoan = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean forcePasswordChange = true; // Force password change on first login

    @Column(name = "account_activated")
    private boolean accountActivated = false;

    @Column(name = "first_login_date")
    private LocalDateTime firstLoginDate;

    @CreationTimestamp
    @Column(name = "date_joined")
    private LocalDateTime dateJoined;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private boolean deleted = false; // Soft delete flag

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Contribution> contributions;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Loan> loans;
}