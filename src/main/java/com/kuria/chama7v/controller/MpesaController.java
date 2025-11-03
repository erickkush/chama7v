package com.kuria.chama7v.controller;

import com.kuria.chama7v.dto.request.MpesaCallbackRequest;
import com.kuria.chama7v.dto.request.MpesaStkRequest;
import com.kuria.chama7v.dto.response.ApiResponse;
import com.kuria.chama7v.dto.response.MpesaResponse;
import com.kuria.chama7v.service.MpesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mpesa")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MpesaController {

    private final MpesaService mpesaService;

    @PostMapping("/stk-push")
    public ResponseEntity<ApiResponse<MpesaResponse>> initiateSTKPush(
            @Valid @RequestBody MpesaStkRequest request) {
        try {
            log.info("STK Push request received for phone: {}", request.getPhoneNumber());
            MpesaResponse response = mpesaService.initiateSTKPush(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("STK Push initiated successfully", response));
        } catch (Exception e) {
            log.error("Error initiating STK Push: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to initiate payment: " + e.getMessage()));
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody MpesaCallbackRequest callbackRequest) {
        try {
            log.info("M-Pesa callback received");
            mpesaService.handleCallback(callbackRequest);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing M-Pesa callback: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }

    @PostMapping("/timeout")
    public ResponseEntity<String> handleTimeout(@RequestBody Map<String, Object> timeoutRequest) {
        log.warn("M-Pesa timeout received: {}", timeoutRequest);
        return ResponseEntity.ok("OK");
    }

    // Test endpoint to verify M-Pesa connectivity
    @GetMapping("/test-token")
    public ResponseEntity<ApiResponse<String>> testAccessToken() {
        try {
            String token = mpesaService.getAccessToken();
            return ResponseEntity.ok(ApiResponse.success("Access token obtained successfully",
                    token.substring(0, Math.min(20, token.length())) + "..."));
        } catch (Exception e) {
            log.error("Failed to get access token: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get access token: " + e.getMessage()));
        }
    }
}