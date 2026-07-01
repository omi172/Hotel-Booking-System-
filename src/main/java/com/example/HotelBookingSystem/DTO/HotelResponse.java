package com.example.HotelBookingSystem.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponse {

    private String id;
    private String name;
    private String city;
    private String address;
    private String description;
    private Double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
