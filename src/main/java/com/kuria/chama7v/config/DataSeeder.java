package com.kuria.chama7v.config;

import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.MemberRole;
import com.kuria.chama7v.entity.enums.MemberStatus;
import com.kuria.chama7v.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        boolean exists = memberRepository.existsByRole(MemberRole.CHAIRPERSON);

        if (!exists) {
            Member admin = new Member();
            admin.setName("Default Chairperson");
            admin.setEmail("admin@chama7v.com");
            admin.setPhone("0700000000");
            admin.setNationalId("12345678");
            admin.setRole(MemberRole.CHAIRPERSON);

            admin.setMemberNumber(generateSequentialMemberNumber());

            // encode the password
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setTotalContributions(BigDecimal.ZERO);
            admin.setOutstandingLoan(BigDecimal.ZERO);

            admin.setStatus(MemberStatus.active);
            admin.setAccountActivated(true);
            admin.setForcePasswordChange(false);

            memberRepository.save(admin);
            System.out.println("Default Chairperson created: email=admin@chama7v.com, password=admin123");
        }
    }

    private String generateSequentialMemberNumber() {
        Integer maxNumber = memberRepository.findMaxMemberNumber();
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("C%03d", nextNumber);
    }
}
