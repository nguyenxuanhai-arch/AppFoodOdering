package com.example.android_lab.services;

public class PaymentResponse {
    private String userId;
    private String orderId;
    private int amount;
    private String note;
    private String qrImageUrl;
    private long expiredAt;

    public String getQrImageUrl() {
        return qrImageUrl;
    }

    // Getter khác nếu cần

    public String getUserId() {
        return userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

}

