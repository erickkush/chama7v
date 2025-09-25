package com.kuria.chama7v.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String memberName, String resetLink);
    void sendWelcomeEmail(String to, String memberName, String memberNumber);
    void sendLoanApprovalEmail(String to, String memberName, String loanNumber);
    void sendLoanRejectionEmail(String to, String memberName, String loanNumber, String reason);

    void sendCredentialsEmail(String to, String memberName, String username, String temporaryPassword);
}