package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:chama7v}")
    private String appName;

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String memberName, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("Password Reset - " + appName);

            String htmlContent = buildPasswordResetEmailTemplate(memberName, resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Async
    @Override
    public void sendCredentialsEmail(String to, String memberName, String username, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("Welcome to " + appName + " - Account Credentials");

            String htmlContent = buildCredentialsEmailTemplate(memberName, username, temporaryPassword);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Credentials email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send credentials email to: {}", to, e);
            throw new RuntimeException("Failed to send credentials email", e);
        }
    }

    @Async
    @Override
    public void sendWelcomeEmail(String to, String memberName, String memberNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Welcome to " + appName);
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Welcome to %s! Your member number is %s.\n\n" +
                            "You can now start making contributions and applying for loans through our platform.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    memberName, appName, memberNumber, appName
            ));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Async
    @Override
    public void sendLoanApprovalEmail(String to, String memberName, String loanNumber) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Loan Approved - " + appName);
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Congratulations! Your loan application %s has been approved.\n\n" +
                            "Please log in to your account for more details.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    memberName, loanNumber, appName
            ));

            mailSender.send(message);
            log.info("Loan approval email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send loan approval email to: {}", to, e);
        }
    }

    @Async
    @Override
    public void sendLoanRejectionEmail(String to, String memberName, String loanNumber, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Loan Application Update - " + appName);
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "We regret to inform you that your loan application %s has been rejected.\n\n" +
                            "Reason: %s\n\n" +
                            "Please feel free to contact us for more information or to discuss your options.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    memberName, loanNumber, reason, appName
            ));

            mailSender.send(message);
            log.info("Loan rejection email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send loan rejection email to: {}", to, e);
        }
    }

    private String buildPasswordResetEmailTemplate(String memberName, String resetLink) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        margin: 0; 
                        padding: 0; 
                        background-color: #f4f4f4; 
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        background-color: #ffffff; 
                        border-radius: 8px; 
                        overflow: hidden; 
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); 
                    }
                    .header { 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center; 
                    }
                    .header h1 { 
                        margin: 0; 
                        font-size: 24px; 
                        font-weight: 600; 
                    }
                    .content { 
                        padding: 30px 20px; 
                        background: #ffffff; 
                    }
                    .content h2 { 
                        color: #333; 
                        margin-bottom: 20px; 
                        font-size: 20px; 
                    }
                    .button { 
                        display: inline-block; 
                        padding: 15px 30px; 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        color: white !important; 
                        text-decoration: none; 
                        border-radius: 5px; 
                        margin: 20px 0; 
                        font-weight: 600; 
                        font-size: 16px; 
                        transition: transform 0.2s ease; 
                    }
                    .button:hover { 
                        transform: translateY(-2px); 
                    }
                    .footer { 
                        text-align: center; 
                        padding: 20px; 
                        font-size: 12px; 
                        color: #666; 
                        background-color: #f8f9fa; 
                    }
                    .warning { 
                        background-color: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 15px 0; 
                        border-radius: 4px; 
                    }
                    .security-tips { 
                        background-color: #d1ecf1; 
                        border-left: 4px solid #17a2b8; 
                        padding: 15px; 
                        margin: 15px 0; 
                        border-radius: 4px; 
                    }
                    .security-tips ul { 
                        margin: 10px 0; 
                        padding-left: 20px; 
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <p>Secure Password Reset</p>
                    </div>
                    <div class="content">
                        <h2>Password Reset Request</h2>
                        <p>Dear %s,</p>
                        <p>We received a request to reset your password for your %s account. If you made this request, click the button below to create a new password:</p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">Reset Your Password</a>
                        </div>
                        
                        <div class="warning">
                            <p><strong>Important:</strong></p>
                            <ul>
                                <li>This link will expire in 24 hours for security reasons</li>
                                <li>If you didn't request this password reset, please ignore this email</li>
                                <li>Do not share this link with anyone</li>
                            </ul>
                        </div>
                        
                        <div class="security-tips">
                            <p><strong>Security Tips:</strong></p>
                            <ul>
                                <li>Use a strong password with at least 8 characters</li>
                                <li>Include uppercase, lowercase, numbers, and special characters</li>
                                <li>Don't reuse passwords from other accounts</li>
                                <li>Enable two-factor authentication when available</li>
                            </ul>
                        </div>
                        
                        <p>If the button doesn't work, you can copy and paste the following link into your browser:</p>
                        <p style="word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 4px; font-family: monospace;">%s</p>
                        
                        <p>If you have any questions or concerns, please contact our support team.</p>
                        
                        <p>Best regards,<br>
                        The %s Security Team</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 %s. All rights reserved.</p>
                        <p>This is an automated email. Please do not reply to this message.</p>
                    </div>
                </div>
            </body>
            </html>
            """, appName, memberName, appName, resetLink, resetLink, appName, appName);
    }

    private String buildCredentialsEmailTemplate(String memberName, String username, String temporaryPassword) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Account Credentials</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        margin: 0; 
                        padding: 0; 
                        background-color: #f4f4f4; 
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        background-color: #ffffff; 
                        border-radius: 8px; 
                        overflow: hidden; 
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); 
                    }
                    .header { 
                        background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); 
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center; 
                    }
                    .header h1 { 
                        margin: 0; 
                        font-size: 24px; 
                        font-weight: 600; 
                    }
                    .content { 
                        padding: 30px 20px; 
                        background: #ffffff; 
                    }
                    .content h2 { 
                        color: #333; 
                        margin-bottom: 20px; 
                        font-size: 20px; 
                    }
                    .credentials { 
                        background: #f8f9fa; 
                        padding: 20px; 
                        border-radius: 8px; 
                        margin: 20px 0; 
                        border-left: 4px solid #28a745; 
                    }
                    .credentials p { 
                        margin: 10px 0; 
                        font-family: monospace; 
                        font-size: 16px; 
                    }
                    .credentials strong { 
                        color: #495057; 
                    }
                    .footer { 
                        text-align: center; 
                        padding: 20px; 
                        font-size: 12px; 
                        color: #666; 
                        background-color: #f8f9fa; 
                    }
                    .warning { 
                        background-color: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 20px; 
                        margin: 20px 0; 
                        border-radius: 4px; 
                    }
                    .warning h3 { 
                        margin-top: 0; 
                        color: #856404; 
                    }
                    .warning ul { 
                        margin: 10px 0; 
                        padding-left: 20px; 
                    }
                    .step-by-step { 
                        background-color: #d1ecf1; 
                        border-left: 4px solid #17a2b8; 
                        padding: 20px; 
                        margin: 20px 0; 
                        border-radius: 4px; 
                    }
                    .step-by-step h3 { 
                        margin-top: 0; 
                        color: #0c5460; 
                    }
                    .step-by-step ol { 
                        margin: 10px 0; 
                        padding-left: 20px; 
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to %s!</h1>
                        <p>Your account has been created</p>
                    </div>
                    <div class="content">
                        <h2>Your Account Credentials</h2>
                        <p>Dear %s,</p>
                        <p>Welcome to %s! Your account has been successfully created by an administrator. Here are your login credentials:</p>
                        
                        <div class="credentials">
                            <p><strong>Username (Email):</strong> %s</p>
                            <p><strong>Temporary Password:</strong> %s</p>
                            <p><strong>Account Status:</strong> PENDING (will activate on first login)</p>
                        </div>
                        
                        <div class="step-by-step">
                            <h3>Next Steps:</h3>
                            <ol>
                                <li>Visit the login page</li>
                                <li>Enter your email and temporary password</li>
                                <li>Your account will be automatically activated</li>
                                <li>Change your password immediately for security</li>
                                <li>Complete your profile setup</li>
                            </ol>
                        </div>
                        
                        <div class="warning">
                            <h3>Important Security Notice:</h3>
                            <ul>
                                <li><strong>Change your password immediately</strong> after your first login</li>
                                <li>Do not share these credentials with anyone</li>
                                <li>Your account status is PENDING until your first successful login</li>
                                <li>Use a strong password with at least 8 characters</li>
                                <li>Include uppercase, lowercase, numbers, and special characters</li>
                                <li>Keep your login credentials secure</li>
                            </ul>
                        </div>
                        
                        <p>Once you log in successfully, your account will be automatically activated and you can:</p>
                        <ul>
                            <li>Make contributions to the group</li>
                            <li>Apply for loans</li>
                            <li>View your contribution history</li>
                            <li>Access member reports</li>
                            <li>Update your profile information</li>
                        </ul>
                        
                        <p>If you have any questions or need assistance, please contact our support team.</p>
                        
                        <p>Best regards,<br>
                        The %s Team</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 %s. All rights reserved.</p>
                        <p>This is an automated email. Please do not reply to this message.</p>
                    </div>
                </div>
            </body>
            </html>
            """, appName, memberName, appName, username, temporaryPassword, appName, appName);
    }
}