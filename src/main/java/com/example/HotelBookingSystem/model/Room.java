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
@Document(collection = "rooms")
public class Room {

    @Id
    private String id;

    private String hotelId;

    private String roomNumber;

    private String type;  // SINGLE, DOUBLE, DELUXE, SUITE

    private Double price;

    private boolean available;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}


