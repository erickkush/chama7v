package com.kuria.chama7v.service;

import com.kuria.chama7v.dto.request.LoginRequest;
import com.kuria.chama7v.dto.request.PasswordResetRequest;
import com.kuria.chama7v.dto.response.JwtResponse;

public interface AuthService {
    JwtResponse login(LoginRequest loginRequest);
    void initiatePasswordReset(PasswordResetRequest request);
    void resetPassword(String token, String newPassword);
    void changePassword(String currentPassword, String newPassword);
    void logout(String token);
}