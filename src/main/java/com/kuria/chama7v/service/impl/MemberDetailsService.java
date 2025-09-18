package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.MemberStatus;
import com.kuria.chama7v.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
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
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found with email: " + email));

        return User.builder()
                .username(member.getEmail())   // email is the username
                .password(member.getPassword())
                .authorities(getAuthorities(member))
                .accountExpired(false)
                .accountLocked(member.getStatus() == MemberStatus.SUSPENDED)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Member member) {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    }
}
