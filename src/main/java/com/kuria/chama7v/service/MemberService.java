package com.kuria.chama7v.service;

import com.kuria.chama7v.dto.request.MemberRegistrationRequest;
import com.kuria.chama7v.dto.response.MemberResponse;
import com.kuria.chama7v.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    MemberResponse registerMember(MemberRegistrationRequest request);
    Page<MemberResponse> getAllMembers(Pageable pageable);
    MemberResponse getMemberById(Long id);
    MemberResponse getMemberByEmail(String email);
    MemberResponse updateMember(Long id, MemberRegistrationRequest request);
    void suspendMember(Long id);
    void activateMember(Long id);
    Page<MemberResponse> searchMembers(String searchTerm, Pageable pageable);
    Member getCurrentMember();
}