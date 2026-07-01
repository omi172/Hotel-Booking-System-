package com.example.HotelBookingSystem.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String bookingId;

    private String userId;

    private Double amount;

    private PaymentStatus paymentStatus;

    private String paymentMethod;  // CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING

    private String transactionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}


