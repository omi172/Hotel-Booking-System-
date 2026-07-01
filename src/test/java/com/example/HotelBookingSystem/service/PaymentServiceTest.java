package com.example.HotelBookingSystem.service;
import com.example.HotelBookingSystem.DTO.PaymentRequest;
import com.example.HotelBookingSystem.DTO.PaymentResponse;
import com.example.HotelBookingSystem.model.*;
import com.example.HotelBookingSystem.repository.BookingRepository;
import com.example.HotelBookingSystem.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private PaymentRequest paymentRequest;
    private Booking booking;

    @BeforeEach
    void setUp() {
        booking = Booking.builder()
                .id("booking123")
                .userId("user@test.com")
                .hotelId("hotel123")
                .roomId("room123")
                .status(BookingStatus.CONFIRMED)
                .build();

        payment = Payment.builder()
                .id("payment123")
                .bookingId("booking123")
                .userId("user@test.com")
                .amount(5000.0)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentMethod("UPI")
                .transactionId("TXN-ABC123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId("booking123");
        paymentRequest.setAmount(5000.0);
        paymentRequest.setPaymentMethod("UPI");
    }

    @Test
    @DisplayName("Should make payment successfully")
    void makePayment_Success() {
        when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingIdAndPaymentStatus("booking123", PaymentStatus.SUCCESS))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.makePayment(paymentRequest, "user@test.com");

        assertThat(response).isNotNull();
        assertThat(response.getBookingId()).isEqualTo("booking123");
        assertThat(response.getAmount()).isEqualTo(5000.0);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when booking not found")
    void makePayment_BookingNotFound() {
        when(bookingRepository.findById("booking123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.makePayment(paymentRequest, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    @DisplayName("Should throw exception when booking belongs to another user")
    void makePayment_AccessDenied() {
        when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.makePayment(paymentRequest, "other@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("Should throw exception for cancelled booking")
    void makePayment_CancelledBooking() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.makePayment(paymentRequest, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cancelled booking");
    }

    @Test
    @DisplayName("Should throw exception when payment already completed")
    void makePayment_AlreadyPaid() {
        when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingIdAndPaymentStatus("booking123", PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.makePayment(paymentRequest, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already completed");
    }

    @Test
    @DisplayName("Should get user payments")
    void getMyPayments_Success() {
        when(paymentRepository.findByUserId("user@test.com")).thenReturn(List.of(payment));

        List<PaymentResponse> payments = paymentService.getMyPayments("user@test.com");

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getUserId()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Should retry failed payment")
    void retryPayment_Success() {
        payment.setPaymentStatus(PaymentStatus.FAILED);
        when(paymentRepository.findById("payment123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.retryPayment("payment123", "user@test.com");

        assertThat(response).isNotNull();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when retrying successful payment")
    void retryPayment_AlreadySuccess() {
        when(paymentRepository.findById("payment123")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.retryPayment("payment123", "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already succeeded");
    }
}

