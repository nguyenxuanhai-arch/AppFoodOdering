package com.example.android_lab.services;

public class PaymentRequest {
    private String userId;
    private String orderId;
    private int amount;
    private String note;
    private final long expiredAt;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PaymentRequest(String userId, String orderId, int amount, String note, long expiredAt) {
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.note = note;
        this.expiredAt = expiredAt;
    }
    public long getExpiredAt() {
        return expiredAt;
    }
}
