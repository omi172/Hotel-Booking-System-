package com.example.HotelBookingSystem.service;
import com.example.HotelBookingSystem.DTO.PaymentRequest;
import com.example.HotelBookingSystem.DTO.PaymentResponse;
import com.example.HotelBookingSystem.model.Booking;
import com.example.HotelBookingSystem.model.BookingStatus;
import com.example.HotelBookingSystem.model.Payment;
import com.example.HotelBookingSystem.model.PaymentStatus;
import com.example.HotelBookingSystem.repository.BookingRepository;
import com.example.HotelBookingSystem.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    // MAKE PAYMENT - Simulates payment processing
    public PaymentResponse makePayment(PaymentRequest request, String userId) {
        // Validate booking exists
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + request.getBookingId()));

        // Validate booking belongs to user
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied. This booking does not belong to you.");
        }

        // Validate booking is CONFIRMED (not cancelled)
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot process payment for a cancelled booking.");
        }

        // Check if payment already succeeded for this booking
        if (paymentRepository.findByBookingIdAndPaymentStatus(request.getBookingId(), PaymentStatus.SUCCESS).isPresent()) {
            throw new RuntimeException("Payment already completed for this booking.");
        }

        // Simulate payment processing (90% success rate)
        PaymentStatus status = simulatePayment();
        String transactionId = generateTransactionId();

        // Create payment record
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .userId(userId)
                .amount(request.getAmount())
                .paymentStatus(status)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Build response
        PaymentResponse response = mapToResponse(savedPayment);
        if (status == PaymentStatus.SUCCESS) {
            response.setMessage("Payment successful! Booking confirmed.");
        } else {
            response.setMessage("Payment failed. Please retry.");
        }

        return response;
    }

    // RETRY FAILED PAYMENT
    public PaymentResponse retryPayment(String paymentId, String userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        // Validate ownership
        if (!payment.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied. This payment does not belong to you.");
        }

        // Only retry failed payments
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment already succeeded. No retry needed.");
        }

        // Simulate retry
        PaymentStatus status = simulatePayment();
        payment.setPaymentStatus(status);
        payment.setTransactionId(generateTransactionId());
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);

        PaymentResponse response = mapToResponse(updatedPayment);
        if (status == PaymentStatus.SUCCESS) {
            response.setMessage("Retry successful! Payment completed.");
        } else {
            response.setMessage("Retry failed. Please try again later.");
        }

        return response;
    }

    // GET MY PAYMENTS
    public List<PaymentResponse> getMyPayments(String userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET PAYMENT BY ID
    public PaymentResponse getPaymentById(String paymentId, String userId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        if (!isAdmin && !payment.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied. You can only view your own payments.");
        }

        return mapToResponse(payment);
    }

    // GET ALL PAYMENTS - Admin only
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET PAYMENTS BY BOOKING ID
    public List<PaymentResponse> getPaymentsByBookingId(String bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Simulate payment (90% success rate)
    private PaymentStatus simulatePayment() {
        Random random = new Random();
        int chance = random.nextInt(100);
        return chance < 90 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }

    // Generate unique transaction ID
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    // Helper method
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}


