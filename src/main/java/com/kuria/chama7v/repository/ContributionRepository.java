package com.kuria.chama7v.repository;

import com.kuria.chama7v.entity.Contribution;
import com.kuria.chama7v.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long>, JpaSpecificationExecutor<Contribution> {

    @Query("SELECT SUM(c.amount) FROM Contribution c")
    BigDecimal getTotalContributions();

    @Query("SELECT SUM(c.amount) FROM Contribution c WHERE c.member = :member")
    BigDecimal getTotalContributionsByMember(@Param("member") Member member);

    @Query("SELECT SUM(c.amount) FROM Contribution c WHERE c.contributionDate BETWEEN :start AND :end")
    BigDecimal getTotalContributionsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<Contribution> findByMemberOrderByContributionDateDesc(Member member, Pageable pageable);

    @Query("SELECT c FROM Contribution c WHERE c.contributionDate BETWEEN :start AND :end")
    Page<Contribution> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
