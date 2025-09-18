package com.kuria.chama7v.repository;

import com.kuria.chama7v.entity.Loan;
import com.kuria.chama7v.entity.LoanPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanOrderByPaymentDateDesc(Loan loan);

    Page<LoanPayment> findByLoanOrderByPaymentDateDesc(Loan loan, Pageable pageable);

    @Query("SELECT SUM(lp.amount) FROM LoanPayment lp WHERE lp.loan = :loan")
    BigDecimal getTotalPaymentsByLoan(@Param("loan") Loan loan);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.paymentDate BETWEEN :startDate AND :endDate ORDER BY lp.paymentDate DESC")
    Page<LoanPayment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);
}