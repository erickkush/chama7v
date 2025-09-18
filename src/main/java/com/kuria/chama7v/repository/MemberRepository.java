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

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMemberNumber(String memberNumber);

    boolean existsByEmail(String email);

    boolean existsByRole(MemberRole role);
    boolean existsByMemberNumber(String memberNumber);

    boolean existsByNationalId(String nationalId);

    List<Member> findByRole(MemberRole role);

    Page<Member> findByStatus(MemberStatus status, Pageable pageable);

    @Query("SELECT SUM(m.totalContributions) FROM Member m WHERE m.status = 'ACTIVE'")
    BigDecimal getTotalGroupContributions();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = 'ACTIVE'")
    Long getActiveMemberCount();

    @Query("SELECT m FROM Member m WHERE m.name LIKE %:searchTerm% OR m.email LIKE %:searchTerm% OR m.memberNumber LIKE %:searchTerm%")
    Page<Member> searchMembers(@Param("searchTerm") String searchTerm, Pageable pageable);
}