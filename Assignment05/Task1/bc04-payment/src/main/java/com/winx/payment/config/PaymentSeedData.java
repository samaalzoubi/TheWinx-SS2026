package com.winx.payment.config;

import com.winx.payment.domain.model.Payment;
import com.winx.payment.domain.service.PaymentProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentSeedData implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PaymentSeedData.class);

    private final PaymentProcessingService paymentProcessingService;

    public PaymentSeedData(PaymentProcessingService paymentProcessingService) {
        this.paymentProcessingService = paymentProcessingService;
    }

    @Override
    public void run(String... args) {
        Payment succeeded = paymentProcessingService.initiatePayment(
                1001L, new BigDecimal("25.00"), "EUR", 1L, "CARD", "****4242");
        log.info("Seed payment created: id={}, bookingId={}, status={}",
                succeeded.getId(), succeeded.getBookingId(), succeeded.getResult().getStatus());

        Payment failed = paymentProcessingService.initiatePayment(
                1002L, new BigDecimal("15.00"), "EUR", 2L, "CARD", "FAIL-TEST");
        log.info("Seed payment created: id={}, bookingId={}, status={}",
                failed.getId(), failed.getBookingId(), failed.getResult().getStatus());

        Payment succeeded2 = paymentProcessingService.initiatePayment(
                1003L, new BigDecimal("42.50"), "EUR", 3L, "PAYPAL", "user3@example.com");
        log.info("Seed payment created: id={}, bookingId={}, status={}",
                succeeded2.getId(), succeeded2.getBookingId(), succeeded2.getResult().getStatus());
    }
}
