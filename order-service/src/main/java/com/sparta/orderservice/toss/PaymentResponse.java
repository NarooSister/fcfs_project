package com.sparta.orderservice.toss;

public class PaymentResponse {
    private String paymentKey;
    private int amount;
    private String status;

    public PaymentResponse(String paymentKey, int amount, String status) {
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.status = status;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public int getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSuccessful() {
        return "COMPLETED".equalsIgnoreCase(status);
    }
}
