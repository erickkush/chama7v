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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

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

    @Value("${mpesa.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortCode;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.callback-url}")
    private String callbackUrl;

    @Value("${mpesa.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public MpesaResponse initiateSTKPush(MpesaStkRequest request) {
        try {
            String accessToken = getAccessToken();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = Base64.getEncoder().encodeToString(
                    (shortCode + passkey + timestamp).getBytes());

            Map<String, Object> stkRequest = new HashMap<>();
            stkRequest.put("BusinessShortCode", shortCode);
            stkRequest.put("Password", password);
            stkRequest.put("Timestamp", timestamp);
            stkRequest.put("TransactionType", "CustomerPayBillOnline");
            stkRequest.put("Amount", request.getAmount().intValue());
            stkRequest.put("PartyA", request.getPhoneNumber());
            stkRequest.put("PartyB", shortCode);
            stkRequest.put("PhoneNumber", request.getPhoneNumber());
            stkRequest.put("CallBackURL", callbackUrl);
            stkRequest.put("AccountReference", request.getAccountReference());
            stkRequest.put("TransactionDesc", request.getTransactionDesc());


            WebClient webClient = webClientBuilder.build();
            Map<String, Object> response = webClient.post()
                    .uri(baseUrl + "/mpesa/stkpush/v1/processrequest")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(stkRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Save transaction record
            MpesaTransaction transaction = new MpesaTransaction();
            transaction.setCheckoutRequestId((String) response.get("CheckoutRequestID"));
            transaction.setMerchantRequestId((String) response.get("MerchantRequestID"));
            transaction.setPhoneNumber(request.getPhoneNumber());
            transaction.setAmount(request.getAmount());
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setTransactionType(request.getTransactionType());

            transactionRepository.save(transaction);

            MpesaResponse mpesaResponse = new MpesaResponse();
            mpesaResponse.setMerchantRequestID((String) response.get("MerchantRequestID"));
            mpesaResponse.setCheckoutRequestID((String) response.get("CheckoutRequestID"));
            mpesaResponse.setResponseCode((String) response.get("ResponseCode"));
            mpesaResponse.setResponseDescription((String) response.get("ResponseDescription"));
            mpesaResponse.setCustomerMessage((String) response.get("CustomerMessage"));

            log.info("STK Push initiated: {}", mpesaResponse.getCheckoutRequestID());

            return mpesaResponse;

        } catch (Exception e) {
            log.error("Error initiating STK Push: ", e);
            throw new RuntimeException("Failed to initiate M-Pesa payment", e);
        }
    }

    @Override
    @Transactional
    public void handleCallback(MpesaCallbackRequest callbackRequest) {
        try {
            MpesaCallbackRequest.StkCallback stkCallback = callbackRequest.getBody().getStkCallback();

            MpesaTransaction transaction = transactionRepository
                    .findByCheckoutRequestId(stkCallback.getCheckoutRequestID())
                    .orElse(null);

            if (transaction == null) {
                log.warn("Transaction not found for CheckoutRequestID: {}", stkCallback.getCheckoutRequestID());
                return;
            }

            transaction.setResultCode(stkCallback.getResultCode().toString());
            transaction.setResultDesc(stkCallback.getResultDesc());

            if (stkCallback.getResultCode() == 0) {
                // Success
                transaction.setStatus(TransactionStatus.SUCCESS);

                if (stkCallback.getCallbackMetadata() != null &&
                        stkCallback.getCallbackMetadata().getItem() != null) {

                    for (MpesaCallbackRequest.Item item : stkCallback.getCallbackMetadata().getItem()) {
                        if ("MpesaReceiptNumber".equals(item.getName())) {
                            transaction.setMpesaReceiptNumber(item.getValue().toString());
                        } else if ("TransactionDate".equals(item.getName())) {
                            // Parse transaction date
                            String dateStr = item.getValue().toString();
                            if (dateStr.length() >= 14) {
                                LocalDateTime transactionDate = LocalDateTime.parse(dateStr,
                                        DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                                transaction.setTransactionDate(transactionDate);
                            }
                        }
                    }
                }

                // Process the payment based on transaction type
                processPayment(transaction);

            } else {
                // Failed
                transaction.setStatus(TransactionStatus.FAILED);
            }

            transactionRepository.save(transaction);

            log.info("Callback processed for transaction: {}", transaction.getCheckoutRequestId());

        } catch (Exception e) {
            log.error("Error processing M-Pesa callback: ", e);
        }
    }

    private void processPayment(MpesaTransaction transaction) {
        // This method would integrate with ContributionService or LoanService
        // to create the actual contribution or loan payment record
        // Implementation depends on business logic
        log.info("Processing payment for transaction type: {}", transaction.getTransactionType());
    }

    @Override
    public String getAccessToken() {
        try {
            String credentials = consumerKey + ":" + consumerSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            WebClient webClient = webClientBuilder.build();
            Map<String, Object> response = webClient.get()
                    .uri(baseUrl + "/oauth/v1/generate?grant_type=client_credentials")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return (String) response.get("access_token");

        } catch (Exception e) {
            log.error("Error getting M-Pesa access token: ", e);
            throw new RuntimeException("Failed to get M-Pesa access token", e);
        }
    }
}