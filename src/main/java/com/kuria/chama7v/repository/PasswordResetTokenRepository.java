package com.kuria.chama7v.repository;


import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpiryDateAfter(String token, LocalDateTime now);

    Optional<PasswordResetToken> findByMemberAndUsedFalse(Member member);

    void deleteByExpiryDateBefore(LocalDateTime now);
}