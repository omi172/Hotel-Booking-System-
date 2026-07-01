package com.example.HotelBookingSystem.repository;
import com.example.HotelBookingSystem.model.Payment;
import com.example.HotelBookingSystem.model.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp() {
        payment1 = Payment.builder()
                .bookingId("booking123")
                .userId("user@test.com")
                .amount(5000.0)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentMethod("UPI")
                .transactionId("TXN-ABC123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        payment2 = Payment.builder()
                .bookingId("booking456")
                .userId("user@test.com")
                .amount(3000.0)
                .paymentStatus(PaymentStatus.FAILED)
                .paymentMethod("CREDIT_CARD")
                .transactionId("TXN-DEF456")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find payments by user ID")
    void findByUserId() {
        List<Payment> payments = paymentRepository.findByUserId("user@test.com");

        assertThat(payments).hasSize(2);
    }

    @Test
    @DisplayName("Should find payments by booking ID")
    void findByBookingId() {
        List<Payment> payments = paymentRepository.findByBookingId("booking123");

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getAmount()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("Should find successful payment for booking")
    void findByBookingIdAndPaymentStatus_Success() {
        Optional<Payment> payment = paymentRepository.findByBookingIdAndPaymentStatus(
                "booking123", PaymentStatus.SUCCESS);

        assertThat(payment).isPresent();
        assertThat(payment.get().getTransactionId()).isEqualTo("TXN-ABC123");
    }

    @Test
    @DisplayName("Should return empty when no successful payment exists")
    void findByBookingIdAndPaymentStatus_NotFound() {
        Optional<Payment> payment = paymentRepository.findByBookingIdAndPaymentStatus(
                "booking456", PaymentStatus.SUCCESS);

        assertThat(payment).isEmpty();
    }

    @Test
    @DisplayName("Should find payments by status")
    void findByPaymentStatus() {
        List<Payment> failedPayments = paymentRepository.findByPaymentStatus(PaymentStatus.FAILED);

        assertThat(failedPayments).hasSize(1);
        assertThat(failedPayments.get(0).getBookingId()).isEqualTo("booking456");
    }

    @Test
    @DisplayName("Should save payment")
    void savePayment() {
        Payment newPayment = Payment.builder()
                .bookingId("booking789")
                .userId("admin@test.com")
                .amount(8000.0)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentMethod("NET_BANKING")
                .transactionId("TXN-GHI789")
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(newPayment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualTo(8000.0);
    }
}

