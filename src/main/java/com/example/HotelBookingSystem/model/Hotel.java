package com.example.HotelBookingSystem.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "hotels")
public class Hotel {

    @Id
    private String id;

    private String name;

    private String city;

    private String address;

    private String description;

    private Double rating;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

