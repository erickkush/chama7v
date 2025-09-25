package com.kuria.chama7v.controller;

import com.kuria.chama7v.dto.request.LoginRequest;
import com.kuria.chama7v.dto.request.PasswordResetRequest;
import com.kuria.chama7v.dto.response.ApiResponse;
import com.kuria.chama7v.dto.response.JwtResponse;
import com.kuria.chama7v.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
        } catch (Exception e) {
            log.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid email or password"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            authService.initiatePasswordReset(request);
            return ResponseEntity.ok(ApiResponse.success(
                    "If an account with this email exists, a password reset link has been sent."));
        } catch (Exception e) {
            log.error("Password reset initiation failed", e);
            // Don't reveal whether email exists for security
            return ResponseEntity.ok(ApiResponse.success(
                    "If an account with this email exists, a password reset link has been sent."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam @NotBlank String token,
            @RequestParam @NotBlank String newPassword) {
        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Password reset failed"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestParam @NotBlank String currentPassword,
            @RequestParam @NotBlank String newPassword) {
        try {
            authService.changePassword(currentPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } catch (Exception e) {
            log.error("Password change failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                authService.logout(token.substring(7));
            }
            return ResponseEntity.ok(ApiResponse.success("Logout successful"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.ok(ApiResponse.success("Logout successful")); // Always return success
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader(value = "Authorization") String token) {
        try {
            String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            boolean isValid = authService.validateToken(jwtToken);
            return ResponseEntity.ok(ApiResponse.success("Token validation result", isValid));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("Token validation result", false));
        }
    }
}