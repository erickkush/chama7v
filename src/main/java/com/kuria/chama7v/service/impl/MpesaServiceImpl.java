package com.kuria.chama7v.service.impl;

import com.kuria.chama7v.dto.request.MpesaCallbackRequest;
import com.kuria.chama7v.dto.request.MpesaStkRequest;
import com.kuria.chama7v.dto.response.MpesaResponse;
import com.kuria.chama7v.entity.MpesaTransaction;
import com.kuria.chama7v.entity.enums.TransactionStatus;
import com.kuria.chama7v.repository.MpesaTransactionRepository;
import com.kuria.chama7v.service.MpesaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaServiceImpl implements MpesaService {

    private final MpesaTransactionRepository transactionRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${mpesa.environment:sandbox}")
    private String environment;

    // Sandbox Configuration
    @Value("${mpesa.sandbox.consumer-key:}")
    private String sandboxConsumerKey;

    @Value("${mpesa.sandbox.consumer-secret:}")
    private String sandboxConsumerSecret;

    @Value("${mpesa.sandbox.shortcode:174379}")
    private String sandboxShortCode;

    @Value("${mpesa.sandbox.passkey:bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919}")
    private String sandboxPasskey;

    @Value("${mpesa.sandbox.base-url:https://sandbox.safaricom.co.ke}")
    private String sandboxBaseUrl;

    // Production Configuration
    @Value("${mpesa.production.consumer-key:}")
    private String prodConsumerKey;

    @Value("${mpesa.production.consumer-secret:}")
    private String prodConsumerSecret;

    @Value("${mpesa.production.shortcode:}")
    private String prodShortCode;

    @Value("${mpesa.production.passkey:}")
    private String prodPasskey;

    @Value("${mpesa.production.base-url:https://api.safaricom.co.ke}")
    private String prodBaseUrl;

    // Callback URL
    @Value("${mpesa.callback-url:http://localhost:8080/api/mpesa/callback}")
    private String callbackUrl;

    private String getConsumerKey() {
        return "sandbox".equalsIgnoreCase(environment) ? sandboxConsumerKey : prodConsumerKey;
    }

    private String getConsumerSecret() {
        return "sandbox".equalsIgnoreCase(environment) ? sandboxConsumerSecret : prodConsumerSecret;
    }

    private String getShortCode() {
        return "sandbox".equalsIgnoreCase(environment) ? sandboxShortCode : prodShortCode;
    }

    private String getPasskey() {
        return "sandbox".equalsIgnoreCase(environment) ? sandboxPasskey : prodPasskey;
    }

    private String getBaseUrl() {
        return "sandbox".equalsIgnoreCase(environment) ? sandboxBaseUrl : prodBaseUrl;
    }

    @Override
    @Transactional
    public MpesaResponse initiateSTKPush(MpesaStkRequest request) {
        try {
            log.info("Initiating STK Push for phone: {}, amount: {}", request.getPhoneNumber(), request.getAmount());

            String formattedPhone = formatPhoneNumber(request.getPhoneNumber());
            String accessToken = getAccessToken();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = Base64.getEncoder().encodeToString(
                    (getShortCode() + getPasskey() + timestamp).getBytes());

            Map<String, Object> stkRequest = new HashMap<>();
            stkRequest.put("BusinessShortCode", getShortCode());
            stkRequest.put("Password", password);
            stkRequest.put("Timestamp", timestamp);
            stkRequest.put("TransactionType", "CustomerPayBillOnline");
            stkRequest.put("Amount", request.getAmount().intValue());
            stkRequest.put("PartyA", formattedPhone);
            stkRequest.put("PartyB", getShortCode());
            stkRequest.put("PhoneNumber", formattedPhone);
            stkRequest.put("CallBackURL", callbackUrl);
            stkRequest.put("AccountReference", request.getAccountReference());
            stkRequest.put("TransactionDesc", request.getTransactionDesc());

            log.debug("STK Push Request: {}", stkRequest);

            WebClient webClient = webClientBuilder.build();
            Map<String, Object> response = webClient.post()
                    .uri(getBaseUrl() + "/mpesa/stkpush/v1/processrequest")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(stkRequest)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        log.error("M-Pesa API error: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error body: {}", errorBody);
                                    return Mono.error(new RuntimeException("M-Pesa API error: " + errorBody));
                                });
                    })
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No response from M-Pesa API");
            }

            log.info("STK Push Response: {}", response);

            String responseCode = String.valueOf(response.get("ResponseCode"));
            if (!"0".equals(responseCode)) {
                String errorMessage = String.valueOf(response.get("ResponseDescription"));
                log.error("M-Pesa STK Push failed: {}", errorMessage);
                throw new RuntimeException("M-Pesa request failed: " + errorMessage);
            }

            MpesaTransaction transaction = new MpesaTransaction();
            transaction.setCheckoutRequestId((String) response.get("CheckoutRequestID"));
            transaction.setMerchantRequestId((String) response.get("MerchantRequestID"));
            transaction.setPhoneNumber(formattedPhone);
            transaction.setAmount(request.getAmount());
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setTransactionType(request.getTransactionType());

            transactionRepository.save(transaction);

            MpesaResponse mpesaResponse = new MpesaResponse();
            mpesaResponse.setMerchantRequestID((String) response.get("MerchantRequestID"));
            mpesaResponse.setCheckoutRequestID((String) response.get("CheckoutRequestID"));
            mpesaResponse.setResponseCode(responseCode);
            mpesaResponse.setResponseDescription((String) response.get("ResponseDescription"));
            mpesaResponse.setCustomerMessage((String) response.get("CustomerMessage"));

            log.info("STK Push initiated successfully: {}", mpesaResponse.getCheckoutRequestID());

            return mpesaResponse;

        } catch (Exception e) {
            log.error("Error initiating STK Push: ", e);
            throw new RuntimeException("Failed to initiate M-Pesa payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void handleCallback(MpesaCallbackRequest callbackRequest) {
        try {
            log.info("Received M-Pesa callback");

            if (callbackRequest == null || callbackRequest.getBody() == null) {
                log.warn("Received null callback request");
                return;
            }

            MpesaCallbackRequest.StkCallback stkCallback = callbackRequest.getBody().getStkCallback();

            if (stkCallback == null) {
                log.warn("STK callback is null");
                return;
            }

            MpesaTransaction transaction = transactionRepository
                    .findByCheckoutRequestId(stkCallback.getCheckoutRequestID())
                    .orElse(null);

            if (transaction == null) {
                log.warn("Transaction not found for CheckoutRequestID: {}", stkCallback.getCheckoutRequestID());
                return;
            }

            transaction.setResultCode(String.valueOf(stkCallback.getResultCode()));
            transaction.setResultDesc(stkCallback.getResultDesc());

            if (stkCallback.getResultCode() == 0) {
                transaction.setStatus(TransactionStatus.SUCCESS);

                if (stkCallback.getCallbackMetadata() != null &&
                        stkCallback.getCallbackMetadata().getItem() != null) {

                    for (MpesaCallbackRequest.Item item : stkCallback.getCallbackMetadata().getItem()) {
                        if ("MpesaReceiptNumber".equals(item.getName())) {
                            transaction.setMpesaReceiptNumber(String.valueOf(item.getValue()));
                        } else if ("TransactionDate".equals(item.getName())) {
                            try {
                                String dateStr = String.valueOf(item.getValue());
                                if (dateStr.length() >= 14) {
                                    LocalDateTime transactionDate = LocalDateTime.parse(dateStr,
                                            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                                    transaction.setTransactionDate(transactionDate);
                                }
                            } catch (Exception e) {
                                log.error("Error parsing transaction date", e);
                            }
                        }
                    }
                }

                log.info("M-Pesa payment successful: {}", transaction.getMpesaReceiptNumber());

            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                log.warn("M-Pesa payment failed: {} - {}", stkCallback.getResultCode(), stkCallback.getResultDesc());
            }

            transactionRepository.save(transaction);

        } catch (Exception e) {
            log.error("Error processing M-Pesa callback: ", e);
        }
    }

    @Override
    public String getAccessToken() {
        try {
            String credentials = getConsumerKey() + ":" + getConsumerSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            WebClient webClient = webClientBuilder.build();
            Map<String, Object> response = webClient.get()
                    .uri(getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        log.error("Failed to get access token: {}", clientResponse.statusCode());
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error body: {}", errorBody);
                                    return Mono.error(new RuntimeException("Failed to get access token: " + errorBody));
                                });
                    })
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("access_token")) {
                throw new RuntimeException("Failed to get access token from M-Pesa");
            }

            String accessToken = (String) response.get("access_token");
            log.debug("Successfully obtained M-Pesa access token");
            return accessToken;

        } catch (Exception e) {
            log.error("Error getting M-Pesa access token: ", e);
            throw new RuntimeException("Failed to get M-Pesa access token: " + e.getMessage(), e);
        }
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        phone = phone.replaceAll("\\D", "");

        if (phone.startsWith("254")) {
            return phone;
        } else if (phone.startsWith("0")) {
            return "254" + phone.substring(1);
        } else if (phone.startsWith("7") || phone.startsWith("1")) {
            return "254" + phone;
        }

        throw new IllegalArgumentException("Invalid phone number format: " + phone);
    }
}