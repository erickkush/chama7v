package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.dto.request.ContributionRequest;
import com.kuria.chama7v.dto.response.ContributionResponse;
import com.kuria.chama7v.entity.Contribution;
import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.exception.ResourceNotFoundException;
import com.kuria.chama7v.repository.ContributionRepository;
import com.kuria.chama7v.repository.MemberRepository;
import com.kuria.chama7v.service.ContributionService;
import com.kuria.chama7v.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContributionServiceImpl implements ContributionService {

    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @Override
    @Transactional
    public ContributionResponse makeContribution(ContributionRequest request) {
        Member member = memberService.getCurrentMember();

        Contribution contribution = new Contribution();
        contribution.setMember(member);
        contribution.setAmount(request.getAmount());
        contribution.setDescription(request.getDescription());
        contribution.setTransactionReference(generateTransactionReference());

        Contribution savedContribution = contributionRepository.save(contribution);

        // Update member's total contributions
        member.setTotalContributions(member.getTotalContributions().add(request.getAmount()));
        memberRepository.save(member);

        log.info("Contribution made by member {}: {}", member.getEmail(), request.getAmount());

        return mapToContributionResponse(savedContribution);
    }

    private String generateTransactionReference() {
        return "CNT-" + System.currentTimeMillis();
    }

    @Override
    public Page<ContributionResponse> getMemberContributions(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        return contributionRepository.findByMemberOrderByContributionDateDesc(member, pageable)
                .map(this::mapToContributionResponse);
    }

    @Override
    public Page<ContributionResponse> getAllContributions(Pageable pageable) {
        return contributionRepository.findAll(pageable)
                .map(this::mapToContributionResponse);
    }

    @Override
    public Page<ContributionResponse> getContributionsByDateRange(LocalDateTime startDate,
                                                                  LocalDateTime endDate,
                                                                  Pageable pageable) {
        return contributionRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::mapToContributionResponse);
    }

    @Override
    public ContributionResponse getContributionById(Long id) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution", "id", id));

        return mapToContributionResponse(contribution);
    }

    private ContributionResponse mapToContributionResponse(Contribution contribution) {
        ContributionResponse response = new ContributionResponse();
        response.setId(contribution.getId());
        response.setMemberName(contribution.getMember().getName());
        response.setMemberNumber(contribution.getMember().getMemberNumber());
        response.setAmount(contribution.getAmount());
        response.setTransactionReference(contribution.getTransactionReference());
        response.setMpesaReceiptNumber(contribution.getMpesaReceiptNumber());
        response.setDescription(contribution.getDescription());
        response.setContributionDate(contribution.getContributionDate());
        return response;
    }
}