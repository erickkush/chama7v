package com.kuria.chama7v.repository;

import com.kuria.chama7v.entity.MpesaTransaction;
import com.kuria.chama7v.entity.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MpesaTransactionRepository extends JpaRepository<MpesaTransaction, Long> {

    Optional<MpesaTransaction> findByCheckoutRequestId(String checkoutRequestId);

    Optional<MpesaTransaction> findByMpesaReceiptNumber(String mpesaReceiptNumber);

    Page<MpesaTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<MpesaTransaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status, Pageable pageable);
}