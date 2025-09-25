package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.MemberStatus;
import com.kuria.chama7v.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> {
                    log.error("Member not found with email: {}", email);
                    return new UsernameNotFoundException("Member not found with email: " + email);
                });

        if (member.isDeleted()) {
            log.error("Attempting to authenticate deleted member: {}", email);
            throw new UsernameNotFoundException("Member account has been deleted");
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .authorities(getAuthorities(member))
                .accountExpired(false)
                .accountLocked(member.getStatus() == MemberStatus.pending)
                .credentialsExpired(false)
                .disabled(member.getStatus() == MemberStatus.inactive)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Member member) {
        String roleName = "ROLE_" + member.getRole().name();
        return Collections.singleton(new SimpleGrantedAuthority(roleName));
    }
}