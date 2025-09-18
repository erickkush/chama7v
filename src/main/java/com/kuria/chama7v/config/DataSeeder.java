package com.kuria.chama7v.config;

import com.kuria.chama7v.entity.Member;
import com.kuria.chama7v.entity.enums.MemberRole;
import com.kuria.chama7v.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

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

            // generate memberNumber explicitly
            admin.setMemberNumber("MBR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

            admin.setPassword(passwordEncoder.encode("admin123"));

            memberRepository.save(admin);
            System.out.println("Default Chairperson created: email=admin@chama7v.com, password=admin123");
        }
    }
}
