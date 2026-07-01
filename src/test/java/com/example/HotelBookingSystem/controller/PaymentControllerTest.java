package com.example.HotelBookingSystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.HotelBookingSystem.DTO.PaymentRequest;
import com.example.HotelBookingSystem.DTO.PaymentResponse;
import com.example.HotelBookingSystem.model.PaymentStatus;
import com.example.HotelBookingSystem.config.JwtUtils;
import com.example.HotelBookingSystem.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtUtils jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId("booking123");
        paymentRequest.setAmount(5000.0);
        paymentRequest.setPaymentMethod("UPI");

        paymentResponse = PaymentResponse.builder()
                .id("payment123")
                .bookingId("booking123")
                .userId("user@test.com")
                .amount(5000.0)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentMethod("UPI")
                .transactionId("TXN-ABC123")
                .createdAt(LocalDateTime.now())
                .message("Payment successful!")
                .build();
    }

    @Test
    @DisplayName("Should make payment - 201")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void makePayment_Success() throws Exception {
        when(paymentService.makePayment(any(PaymentRequest.class), eq("user@test.com")))
                .thenReturn(paymentResponse);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("TXN-ABC123"));
    }

    @Test
    @DisplayName("Should get my payments - 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getMyPayments_Success() throws Exception {
        when(paymentService.getMyPayments("user@test.com")).thenReturn(List.of(paymentResponse));

        mockMvc.perform(get("/api/payments/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(5000.0));
    }

    @Test
    @DisplayName("Should retry payment - 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void retryPayment_Success() throws Exception {
        when(paymentService.retryPayment(eq("payment123"), eq("user@test.com")))
                .thenReturn(paymentResponse);

        mockMvc.perform(put("/api/payments/payment123/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }

    @Test
    @DisplayName("Admin should get all payments - 200")
    @WithMockUser(roles = "ADMIN")
    void getAllPayments_Admin_Success() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(paymentResponse));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value("booking123"));
    }

    @Test
    @DisplayName("User should NOT get all payments - 403")
    @WithMockUser(roles = "USER")
    void getAllPayments_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isForbidden());
    }
}


