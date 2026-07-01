package com.example.HotelBookingSystem.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private String id;
    private String hotelId;
    private String roomNumber;
    private String type;
    private Double price;
    private boolean available;
    private String description;
}


