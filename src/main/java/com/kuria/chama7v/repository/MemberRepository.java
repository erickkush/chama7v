package com.kuria.chama7v.repository;

import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.MemberRole;
import com.kuria.chama7v.entity.enums.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndDeletedFalse(String email);

    Optional<Member> findByMemberNumberAndDeletedFalse(String memberNumber);

    Optional<Member> findByIdAndDeletedFalse(Long id);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByRole(MemberRole role);

    boolean existsByRoleAndDeletedFalse(MemberRole role);

    boolean existsByMemberNumberAndDeletedFalse(String memberNumber);

    boolean existsByNationalIdAndDeletedFalse(String nationalId);

    List<Member> findByRoleAndDeletedFalse(MemberRole role);

    Page<Member> findByStatusAndDeletedFalse(MemberStatus status, Pageable pageable);

    Page<Member> findByDeletedFalse(Pageable pageable);

    @Query("SELECT COALESCE(SUM(m.totalContributions), 0) FROM Member m WHERE m.status = 'active' AND m.deleted = false")
    BigDecimal getTotalGroupContributions();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = 'active' AND m.deleted = false")
    Long getActiveMemberCount();

    @Query("SELECT m FROM Member m WHERE m.deleted = false AND (m.name LIKE %:searchTerm% OR m.email LIKE %:searchTerm% OR m.memberNumber LIKE %:searchTerm%)")
    Page<Member> searchMembers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(m.memberNumber, 2) AS int)), 0) FROM Member m WHERE m.memberNumber LIKE 'C%' AND m.deleted = false")
    Integer findMaxMemberNumber();
}