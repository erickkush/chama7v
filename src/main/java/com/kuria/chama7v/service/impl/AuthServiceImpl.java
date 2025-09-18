package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.dto.request.LoginRequest;
import com.kuria.chama7v.dto.request.PasswordResetRequest;
import com.kuria.chama7v.dto.response.JwtResponse;
import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.PasswordResetToken;
import com.kuria.chama7v.exception.ResourceNotFoundException;
import com.kuria.chama7v.repository.MemberRepository;
import com.kuria.chama7v.repository.PasswordResetTokenRepository;
import com.kuria.chama7v.service.AuthService;
import com.kuria.chama7v.service.EmailService;
import com.kuria.chama7v.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("memberId", member.getId());
        extraClaims.put("memberNumber", member.getMemberNumber());
        extraClaims.put("role", member.getRole().name());

        String jwt = jwtUtil.generateToken(loginRequest.getEmail(), extraClaims);

        log.info("Member {} logged in successfully", member.getEmail());

        return new JwtResponse(
                jwt,
                member.getId(),
                member.getMemberNumber(),
                member.getName(),
                member.getEmail(),
                member.getRole(),
                member.getStatus()
        );
    }

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        Member member = getCurrentlyAuthenticatedMember();
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("Current password incorrect");
        }
        member.setPassword(passwordEncoder.encode(newPassword));
        member.setForcePasswordChange(false);
        memberRepository.save(member);
    }

    private Member getCurrentlyAuthenticatedMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = authentication.getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
    }

    @Override
    @Transactional
    public void initiatePasswordReset(PasswordResetRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + request.getEmail()));

        tokenRepository.findByMemberAndUsedFalse(member).ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setMember(member);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(member.getEmail(), token);

        log.info("Password reset initiated for member: {}", member.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository
                .findByTokenAndUsedFalseAndExpiryDateAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        Member member = resetToken.getMember();
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset completed for member: {}", member.getEmail());
    }

    @Override
    public void logout(String token) {
        // Stateless JWT: logout is handled client-side
        log.info("User logged out");
    }
}
