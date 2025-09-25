package com.kuria.chama7v.repository;

import com.kuria.chama7v.entity.Loan;
import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.LoanStatus;
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
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    Optional<Loan> findByLoanNumber(String loanNumber);

    Page<Loan> findByMemberOrderByApplicationDateDesc(Member member, Pageable pageable);

    Page<Loan> findByStatusOrderByApplicationDateDesc(LoanStatus status, Pageable pageable);

    List<Loan> findByMemberAndStatusIn(Member member, List<LoanStatus> statuses);

    @Query("SELECT COALESCE(SUM(l.balance), 0) FROM Loan l WHERE l.status IN ('APPROVED', 'DISBURSED')")
    BigDecimal getTotalOutstandingLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'PENDING'")
    Long getPendingLoanCount();

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM Loan l WHERE l.status IN ('APPROVED', 'DISBURSED', 'PAID')")
    BigDecimal getTotalDisbursedLoans();

    @Query("SELECT l FROM Loan l WHERE l.applicationDate BETWEEN :startDate AND :endDate ORDER BY l.applicationDate DESC")
    Page<Loan> findByDateRange(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate,
                               Pageable pageable);
}