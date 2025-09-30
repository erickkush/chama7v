package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.dto.request.ForgotPasswordRequest;
import com.kuria.chama7v.dto.request.LoginRequest;
import com.kuria.chama7v.dto.request.ResetPasswordRequest;
import com.kuria.chama7v.dto.response.JwtResponse;
import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.PasswordResetToken;
import com.kuria.chama7v.exception.ResourceNotFoundException;
import com.kuria.chama7v.exception.UnauthorizedException;
import com.kuria.chama7v.repository.MemberRepository;
import com.kuria.chama7v.repository.PasswordResetTokenRepository;
import com.kuria.chama7v.service.AuthService;
import com.kuria.chama7v.service.EmailService;
import com.kuria.chama7v.service.MemberService;
import com.kuria.chama7v.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final MemberService memberService;

    @Value("${app.password-reset.token-validity-hours:24}")
    private int tokenValidityHours;

    @Value("${app.frontend.reset-password-url:http://localhost:4200/reset-password}")
    private String resetPasswordUrl;

    @Override
    @Transactional
    public JwtResponse login(LoginRequest loginRequest) {
        logAuthenticationAttempt(loginRequest);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Member member = memberRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Activate account on first successful login
            if (!member.isAccountActivated()) {
                memberService.activateAccount(member.getEmail());
                member = memberRepository.findByEmailAndDeletedFalse(loginRequest.getEmail()).get();
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", member.getRole().name());
            claims.put("status", member.getStatus().name());
            claims.put("memberId", member.getId());
            claims.put("memberNumber", member.getMemberNumber());

            String token = jwtUtil.generateToken(member.getEmail(), claims);

            log.info("Successful login for member: {}", member.getEmail());

            return new JwtResponse(
                    token,
                    member.getId(),
                    member.getMemberNumber(),
                    member.getName(),
                    member.getEmail(),
                    member.getRole(),
                    member.getStatus(),
                    member.isForcePasswordChange()
            );

        } catch (Exception e) {
            log.error("Login failed for email {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        Member member = memberRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Member", "email", request.getEmail()));

        // Invalidate existing tokens for this member
        tokenRepository.invalidateTokensForMember(member);

        // Generate secure token
        String resetToken = generateSecureToken();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setMember(member);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(tokenValidityHours));
        passwordResetToken.setUsed(false);

        tokenRepository.save(passwordResetToken);

        // Send reset email with link
        String resetLink = resetPasswordUrl + "?token=" + resetToken;
        emailService.sendPasswordResetEmail(member.getEmail(), member.getName(), resetLink);

        log.info("Password reset initiated for member: {}", member.getEmail());
    }


    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        Member member = resetToken.getMember();

        // Update password
        member.setPassword(passwordEncoder.encode(newPassword));
        member.setForcePasswordChange(false); // Remove force password change after successful reset
        memberRepository.save(member);

        // Mark token as used
        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for member: {}", member.getEmail());
    }

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        Member currentMember = memberService.getCurrentMember();

        if (!passwordEncoder.matches(currentPassword, currentMember.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        currentMember.setPassword(passwordEncoder.encode(newPassword));
        currentMember.setForcePasswordChange(false);
        memberRepository.save(currentMember);

        log.info("Password changed successfully for member: {}", currentMember.getEmail());
    }

    @Override
    @Transactional
    public void logout(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                jwtUtil.blacklistToken(token);
            }
            SecurityContextHolder.clearContext();
            log.info("User logged out successfully");
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    @Override
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token) && !jwtUtil.isTokenBlacklisted(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.debug("Cleaned up expired password reset tokens");
    }

    private void logAuthenticationAttempt(LoginRequest loginRequest) {
        try {
            Member member = memberRepository.findByEmailAndDeletedFalse(loginRequest.getEmail()).orElse(null);
            if (member != null) {
                log.info("Authentication attempt for member: {} with status: {}, activated: {}",
                        member.getEmail(), member.getStatus(), member.isAccountActivated());
            } else {
                log.warn("Authentication attempt for non-existent email: {}", loginRequest.getEmail());
            }
        } catch (Exception e) {
            log.error("Error during authentication logging", e);
        }
    }
}