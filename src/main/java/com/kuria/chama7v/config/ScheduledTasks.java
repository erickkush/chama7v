package com.kuria.chama7v.config;

import com.kuria.chama7v.service.impl.AuthServiceImpl;
import com.kuria.chama7v.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final AuthServiceImpl authService;
    private final JwtUtil jwtUtil;

    // Clean up expired password reset tokens daily at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        try {
            authService.cleanupExpiredTokens();
            jwtUtil.cleanupBlacklist();
            log.info("Successfully cleaned up expired tokens");
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage());
        }
    }
}