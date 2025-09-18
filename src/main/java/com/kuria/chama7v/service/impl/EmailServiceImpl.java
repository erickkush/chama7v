package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Chama7v - Password Reset Request");
            message.setText(buildPasswordResetEmailBody(resetToken));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: ", to, e);
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String memberName, String memberNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to Chama7v - Your Registration is Complete");
            message.setText(buildWelcomeEmailBody(memberName, memberNumber));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: ", to, e);
        }
    }

    @Override
    public void sendLoanApprovalEmail(String to, String memberName, String loanNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Loan Approved - " + loanNumber);
            message.setText(buildLoanApprovalEmailBody(memberName, loanNumber));

            mailSender.send(message);
            log.info("Loan approval email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send loan approval email to {}: ", to, e);
        }
    }

    @Override
    public void sendLoanRejectionEmail(String to, String memberName, String loanNumber, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Loan Application Update - " + loanNumber);
            message.setText(buildLoanRejectionEmailBody(memberName, loanNumber, reason));

            mailSender.send(message);
            log.info("Loan rejection email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send loan rejection email to {}: ", to, e);
        }
    }

    private String buildPasswordResetEmailBody(String resetToken) {
        return """
                Dear Chama Member,
                
                You have requested to reset your password for your Chama7v account.
                
                Please use the following token to reset your password:
                %s
                
                This token will expire in 24 hours.
                
                If you did not request this password reset, please ignore this email.
                
                Best regards,
                Chama7v Team
                """.formatted(resetToken);
    }

    private String buildWelcomeEmailBody(String memberName, String memberNumber) {
        return """
                Dear %s,
                
                Welcome to Chama7v! Your registration has been completed successfully.
                
                Your member details:
                - Member Number: %s
                - Member Name: %s
                
                You can now log in to your account and start making contributions.
                
                Thank you for joining our chama!
                
                Best regards,
                Chama7v Team
                """.formatted(memberName, memberNumber, memberName);
    }

    private String buildLoanApprovalEmailBody(String memberName, String loanNumber) {
        return """
                Dear %s,
                
                Congratulations! Your loan application has been approved.
                
                Loan Details:
                - Loan Number: %s
                - Status: Approved
                
                The loan amount will be processed for disbursement shortly.
                
                Best regards,
                Chama7v Team
                """.formatted(memberName, loanNumber);
    }
    @Override
    public void sendCredentialsEmail(String to, String memberName, String username, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Chama7v - Your account credentials");

            String body = """
                Dear %s,
                
                An account has been created for you on Chama7v.
                
                Username: %s
                Temporary password: %s
                
                For security reasons, please log in and change your password immediately.
                
                If you did not expect this email, please contact your Chama administrator.
                
                Best regards,
                Chama7v Team
                """.formatted(memberName, username, temporaryPassword);

            message.setText(body);
            mailSender.send(message);
            log.info("Credentials email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send credentials email to {}: ", to, e);
        }
    }


    private String buildLoanRejectionEmailBody(String memberName, String loanNumber, String reason) {
        return """
                Dear %s,
                
                We regret to inform you that your loan application has not been approved.
                
                Loan Details:
                - Loan Number: %s
                - Status: Rejected
                - Reason: %s
                
                You may apply for a new loan after addressing the concerns mentioned above.
                
                Best regards,
                Chama7v Team
                """.formatted(memberName, loanNumber, reason);
    }
}