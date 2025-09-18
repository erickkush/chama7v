package com.kuria.chama7v.dto.response;

import lombok.Data;

@Data
public class MpesaResponse {
    private String merchantRequestID;
    private String checkoutRequestID;
    private String responseCode;
    private String responseDescription;
    private String customerMessage;
}