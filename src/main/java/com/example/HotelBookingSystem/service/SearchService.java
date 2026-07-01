package com.example.HotelBookingSystem.service;

import com.example.HotelBookingSystem.DTO.HotelResponse;
import com.example.HotelBookingSystem.DTO.RoomResponse;
import com.example.HotelBookingSystem.DTO.SearchResponse;
import com.example.HotelBookingSystem.model.Booking;
import com.example.HotelBookingSystem.model.Hotel;
import com.example.HotelBookingSystem.model.Room;
import com.example.HotelBookingSystem.repository.BookingRepository;
import com.example.HotelBookingSystem.repository.Hotelrepository;
import com.example.HotelBookingSystem.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final Hotelrepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public List<SearchResponse> searchAvailableHotels(String city, LocalDate checkIn, LocalDate checkOut) {
        // Validate dates
        if (!checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        // Step 1: Find all hotels in the city
        List<Hotel> hotels = hotelRepository.findByCityIgnoreCase(city);

        if (hotels.isEmpty()) {
            throw new RuntimeException("No hotels found in city: " + city);
        }

        List<SearchResponse> results = new ArrayList<>();

        // Step 2: For each hotel, find available rooms
        for (Hotel hotel : hotels) {
            List<Room> allRooms = roomRepository.findByHotelId(hotel.getId());

            // Step 3: Filter out rooms that have conflicting bookings
            List<Room> availableRooms = allRooms.stream()
                    .filter(room -> isRoomAvailable(room.getId(), checkIn, checkOut))
                    .collect(Collectors.toList());

            // Only include hotels that have available rooms
            if (!availableRooms.isEmpty()) {
                SearchResponse response = SearchResponse.builder()
                        .hotel(mapToHotelResponse(hotel))
                        .availableRooms(availableRooms.stream()
                                .map(this::mapToRoomResponse)
                                .collect(Collectors.toList()))
                        .build();

                results.add(response);
            }
        }

        return results;
    }

    // Check if a room has no conflicting bookings for the given dates
    private boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(roomId, checkIn, checkOut);
        return conflicts.isEmpty();
    }

    // Helper: Map Hotel to HotelResponse
    private HotelResponse mapToHotelResponse(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .address(hotel.getAddress())
                .description(hotel.getDescription())
                .rating(hotel.getRating())
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .build();
    }

    // Helper: Map Room to RoomResponse
    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .hotelId(room.getHotelId())
                .roomNumber(room.getRoomNumber())
                .type(room.getType())
                .price(room.getPrice())
                .available(room.isAvailable())
                .description(room.getDescription())
                .build();
    }
}


