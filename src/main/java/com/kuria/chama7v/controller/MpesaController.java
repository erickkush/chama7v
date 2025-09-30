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

@RestController
@RequestMapping("/api/mpesa")
@RequiredArgsConstructor
@Slf4j
public class MpesaController {

    private final MpesaService mpesaService;

    @PostMapping("/stk-push")
    public ResponseEntity<ApiResponse<MpesaResponse>> initiateSTKPush(
            @Valid @RequestBody MpesaStkRequest request) {
        MpesaResponse response = mpesaService.initiateSTKPush(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("STK Push initiated successfully", response));
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody MpesaCallbackRequest callbackRequest) {
        try {
            mpesaService.handleCallback(callbackRequest);
            log.info("M-Pesa callback processed successfully");
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing M-Pesa callback: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR");
        }
    }
}
