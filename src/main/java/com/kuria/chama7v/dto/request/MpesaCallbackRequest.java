package com.kuria.chama7v.dto.request;

import lombok.Data;

@Data
public class MpesaCallbackRequest {
    private Body body;

    @Data
    public static class Body {
        private StkCallback stkCallback;
    }

    @Data
    public static class StkCallback {
        private String merchantRequestID;
        private String checkoutRequestID;
        private Integer resultCode;
        private String resultDesc;
        private CallbackMetadata callbackMetadata;
    }

    @Data
    public static class CallbackMetadata {
        private Item[] item;
    }

    @Data
    public static class Item {
        private String name;
        private Object value;
    }
}