package com.sparta.fcfsproject.order.service;

public interface RefundPolicy {
    double calculateRefundAmount(int ticketPrice, int quantity);
    class FullRefundPolicy implements RefundPolicy {
        @Override
        public double calculateRefundAmount(int ticketPrice, int quantity) {
            return ticketPrice * quantity;  // 전액 환불
        }
    }

    class TenPercentRefundPolicy implements RefundPolicy {
        @Override
        public double calculateRefundAmount(int ticketPrice, int quantity) {
            return ticketPrice * quantity * 0.9;  // 10% 수수료
        }
    }

    class TwentyPercentRefundPolicy implements RefundPolicy {
        @Override
        public double calculateRefundAmount(int ticketPrice, int quantity) {
            return ticketPrice * quantity * 0.8;  // 20% 수수료
        }
    }

    class ThirtyPercentRefundPolicy implements RefundPolicy {
        @Override
        public double calculateRefundAmount(int ticketPrice, int quantity) {
            return ticketPrice * quantity * 0.7;  // 30% 수수료
        }
    }
}
