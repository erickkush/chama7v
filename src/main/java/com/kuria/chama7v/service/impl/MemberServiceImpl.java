package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.dto.request.MemberRegistrationRequest;
import com.kuria.chama7v.dto.response.MemberResponse;
import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.MemberRole;
import com.kuria.chama7v.entity.enums.MemberStatus;
import com.kuria.chama7v.exception.ResourceNotFoundException;
import com.kuria.chama7v.repository.MemberRepository;
import com.kuria.chama7v.service.EmailService;
import com.kuria.chama7v.service.MemberService;
import com.kuria.chama7v.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationUtil validationUtil;
    private final EmailService emailService;

    @Override
    @Transactional
    public MemberResponse registerMember(MemberRegistrationRequest request) {
        // Validation
        if (memberRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (request.getNationalId() != null &&
                memberRepository.existsByNationalIdAndDeletedFalse(request.getNationalId())) {
            throw new IllegalArgumentException("National ID already exists");
        }

        String formattedPhone = validationUtil.formatPhoneNumber(request.getPhone());
        String tempPassword = generateSecureTemporaryPassword(12);
        String memberNumber = generateSequentialMemberNumber();

        Member member = new Member();
        member.setMemberNumber(memberNumber);
        member.setNationalId(request.getNationalId());
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(formattedPhone);
        member.setPassword(passwordEncoder.encode(tempPassword));
        member.setStatus(MemberStatus.pending); // Start with PENDING status
        member.setRole(request.getRole() != null ? request.getRole() : MemberRole.MEMBER);
        member.setTotalContributions(BigDecimal.ZERO);
        member.setOutstandingLoan(BigDecimal.ZERO);
        member.setForcePasswordChange(true);
        member.setAccountActivated(false);

        Member saved = memberRepository.save(member);

        // Send credentials email
        emailService.sendCredentialsEmail(saved.getEmail(), saved.getName(),
                saved.getEmail(), tempPassword);

        log.info("Member {} registered with pending status", saved.getEmail());
        return mapToMemberResponse(saved);
    }

    private String generateSequentialMemberNumber() {
        Integer maxNumber = memberRepository.findMaxMemberNumber();
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;

        if (nextNumber > 999) {
            throw new IllegalStateException("Maximum member number reached (C999)");
        }

        return String.format("C%03d", nextNumber);
    }


    private String generateSecureTemporaryPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*()-_=+";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public void activateAccount(String email) {
        Member member = memberRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));

        if (!member.isAccountActivated()) {
            member.setAccountActivated(true);
            member.setStatus(MemberStatus.active);
            member.setFirstLoginDate(LocalDateTime.now());
            memberRepository.save(member);
            log.info("Account activated for member: {}", email);
        }
    }

    @Override
    @Transactional
    public MemberResponse updateMember(Long id, MemberRegistrationRequest request) {
        Member member = memberRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        // Check if email is being changed and if new email already exists
        if (!member.getEmail().equals(request.getEmail()) &&
                memberRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if national ID is being changed and if new national ID already exists
        if (!member.getNationalId().equals(request.getNationalId()) &&
                memberRepository.existsByNationalIdAndDeletedFalse(request.getNationalId())) {
            throw new IllegalArgumentException("National ID already exists");
        }

        member.setNationalId(request.getNationalId());
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(validationUtil.formatPhoneNumber(request.getPhone()));

        if (request.getRole() != null) {
            member.setRole(request.getRole());
        }

        Member updatedMember = memberRepository.save(member);
        log.info("Member updated: {}", updatedMember.getEmail());

        return mapToMemberResponse(updatedMember);
    }

    @Override
    @Transactional
    public void suspendMember(Long id) {
        Member member = memberRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        member.setStatus(MemberStatus.suspended);
        memberRepository.save(member);
        log.info("Member suspended: {}", member.getEmail());
    }

    @Override
    @Transactional
    public void activateMember(Long id) {
        Member member = memberRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        member.setStatus(MemberStatus.active);
        memberRepository.save(member);
        log.info("Member activated: {}", member.getEmail());
    }

    @Override
    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        // Soft delete
        member.setDeleted(true);
        member.setStatus(MemberStatus.inactive);
        memberRepository.save(member);
        log.info("Member soft deleted: {}", member.getEmail());
    }

    @Override
    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findByDeletedFalse(pageable)
                .map(this::mapToMemberResponse);
    }

    @Override
    public MemberResponse getMemberById(Long id) {
        Member member = memberRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
        return mapToMemberResponse(member);
    }

    @Override
    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current member not found"));
    }

    @Override
    public Page<MemberResponse> searchMembers(String searchTerm, Pageable pageable) {
        return memberRepository.searchMembers(searchTerm, pageable)
                .map(this::mapToMemberResponse);
    }

    private MemberResponse mapToMemberResponse(Member member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setMemberNumber(member.getMemberNumber());
        response.setNationalId(member.getNationalId());
        response.setName(member.getName());
        response.setEmail(member.getEmail());
        response.setPhone(member.getPhone());
        response.setRole(member.getRole());
        response.setStatus(member.getStatus());
        response.setTotalContributions(member.getTotalContributions());
        response.setOutstandingLoan(member.getOutstandingLoan());
        response.setDateJoined(member.getDateJoined());
        response.setAccountActivated(member.isAccountActivated());
        return response;
    }
}