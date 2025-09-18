package com.kuria.chama7v.service;

import com.kuria.chama7v.dto.request.MpesaCallbackRequest;
import com.kuria.chama7v.dto.request.MpesaStkRequest;
import com.kuria.chama7v.dto.response.MpesaResponse;

public interface MpesaService {
    MpesaResponse initiateSTKPush(MpesaStkRequest request);
    void handleCallback(MpesaCallbackRequest callbackRequest);
    String getAccessToken();
}
