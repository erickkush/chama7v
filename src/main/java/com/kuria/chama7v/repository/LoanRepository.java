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

    @Query("SELECT SUM(l.balance) FROM Loan l WHERE l.status IN ('APPROVED', 'DISBURSED')")
    BigDecimal getTotalOutstandingLoans();

    @Query("SELECT SUM(l.totalAmount) FROM Loan l WHERE l.status = 'DISBURSED'")
    BigDecimal getTotalDisbursedLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = 'PENDING'")
    Long getPendingLoanCount();

    Page<Loan> findByMemberOrderByApplicationDateDesc(Member member, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.applicationDate BETWEEN :start AND :end")
    Page<Loan> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    List<Loan> findByMemberAndStatusIn(Member member, List<LoanStatus> statuses);

    Page<Loan> findByStatusOrderByApplicationDateDesc(LoanStatus status, Pageable pageable);

}
