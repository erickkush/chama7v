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
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (request.getNationalId() != null && memberRepository.existsByNationalId(request.getNationalId())) {
            throw new IllegalArgumentException("National ID already exists");
        }

        String formattedPhone = validationUtil.formatPhoneNumber(request.getPhone());

        // generate secure temporary password (OTP)
        String tempPassword = generateSecureTemporaryPassword(12);

        Member member = new Member();
        member.setMemberNumber(generateUniqueMemberNumber());
        member.setNationalId(request.getNationalId());
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(formattedPhone);
        member.setPassword(passwordEncoder.encode(tempPassword));
        member.setStatus(MemberStatus.ACTIVE);
        member.setRole(request.getRole() != null ? request.getRole() : MemberRole.MEMBER);
        member.setTotalContributions(BigDecimal.ZERO);
        member.setOutstandingLoan(BigDecimal.ZERO);

        // force the user to change password at first login
        member.setForcePasswordChange(true);

        Member saved = memberRepository.save(member);

        // send credentials to new member
        emailService.sendCredentialsEmail(saved.getEmail(), saved.getName(), saved.getEmail(), tempPassword);

        log.info("Member {} registered by admin (sent temporary credentials)", saved.getEmail());
        return mapToMemberResponse(saved);
    }

    // helper
    private String generateSecureTemporaryPassword(int len) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*()-_=+";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateUniqueMemberNumber() {
        String memberNumber;
        do {
            memberNumber = validationUtil.generateMemberNumber();
        } while (memberRepository.existsByMemberNumber(memberNumber));
        return memberNumber;
    }

    @Override
    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(this::mapToMemberResponse);
    }

    @Override
    public MemberResponse getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
        return mapToMemberResponse(member);
    }

    @Override
    public MemberResponse getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
        return mapToMemberResponse(member);
    }

    @Override
    @Transactional
    public MemberResponse updateMember(Long id, MemberRegistrationRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        // Check if email is being changed and if new email already exists
        if (!member.getEmail().equals(request.getEmail()) &&
                memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if national ID is being changed and if new national ID already exists
        if (!member.getNationalId().equals(request.getNationalId()) &&
                memberRepository.existsByNationalId(request.getNationalId())) {
            throw new IllegalArgumentException("National ID already exists");
        }

        member.setNationalId(request.getNationalId());
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(validationUtil.formatPhoneNumber(request.getPhone()));
        member.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            member.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Member updatedMember = memberRepository.save(member);
        log.info("Member updated: {}", updatedMember.getEmail());

        return mapToMemberResponse(updatedMember);
    }

    @Override
    @Transactional
    public void suspendMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        member.setStatus(MemberStatus.SUSPENDED);
        memberRepository.save(member);

        log.info("Member suspended: {}", member.getEmail());
    }

    @Override
    @Transactional
    public void activateMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        member.setStatus(MemberStatus.ACTIVE);
        memberRepository.save(member);

        log.info("Member activated: {}", member.getEmail());
    }

    @Override
    public Page<MemberResponse> searchMembers(String searchTerm, Pageable pageable) {
        return memberRepository.searchMembers(searchTerm, pageable)
                .map(this::mapToMemberResponse);
    }

    @Override
    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current member not found"));
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
        return response;
    }
}