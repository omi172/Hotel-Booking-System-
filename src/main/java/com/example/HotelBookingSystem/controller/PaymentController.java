package com.example.HotelBookingSystem.controller;
import com.example.HotelBookingSystem.DTO.PaymentRequest;
import com.example.HotelBookingSystem.DTO.PaymentResponse;
import com.example.HotelBookingSystem.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // MAKE PAYMENT - Any authenticated user
    @PostMapping
    public ResponseEntity<PaymentResponse> makePayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        PaymentResponse response = paymentService.makePayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // RETRY FAILED PAYMENT
    @PutMapping("/{id}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        PaymentResponse response = paymentService.retryPayment(id, userId);
        return ResponseEntity.ok(response);
    }

    // GET MY PAYMENTS
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication) {
        String userId = authentication.getName();
        List<PaymentResponse> payments = paymentService.getMyPayments(userId);
        return ResponseEntity.ok(payments);
    }

    // GET PAYMENT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        PaymentResponse response = paymentService.getPaymentById(id, userId, isAdmin);
        return ResponseEntity.ok(response);
    }

    // GET PAYMENTS BY BOOKING ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByBooking(
            @PathVariable String bookingId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByBookingId(bookingId);
        return ResponseEntity.ok(payments);
    }

    // GET ALL PAYMENTS - Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
}


