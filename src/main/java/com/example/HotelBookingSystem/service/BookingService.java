package com.example.HotelBookingSystem.service;
import com.example.HotelBookingSystem.DTO.BookingRequest;
import com.example.HotelBookingSystem.DTO.BookingResponse;
import com.example.HotelBookingSystem.model.Booking;
import com.example.HotelBookingSystem.model.BookingStatus;
import com.example.HotelBookingSystem.model.Hotel;
import com.example.HotelBookingSystem.repository.BookingRepository;
import com.example.HotelBookingSystem.repository.Hotelrepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final Hotelrepository hotelRepository;

    // CREATE BOOKING - with date conflict check
    public BookingResponse createBooking(BookingRequest request, String userId) {
        // Validate check-out is after check-in
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        // Validate hotel exists
        hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + request.getHotelId()));

        // Check for date conflicts
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Room is already booked for the selected dates. Please choose different dates.");
        }

        // Create booking
        Booking booking = Booking.builder()
                .userId(userId)
                .hotelId(request.getHotelId())
                .roomId(request.getRoomId())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        BookingResponse response = mapToResponse(savedBooking);
        response.setMessage("Booking confirmed successfully!");
        return response;
    }

    // GET MY BOOKINGS - User's own bookings
    public List<BookingResponse> getMyBookings(String userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET ALL BOOKINGS - Admin only
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET BOOKING BY ID
    public BookingResponse getBookingById(String bookingId, String userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Only allow user to view their own booking (unless admin)
        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied. You can only view your own bookings.");
        }

        return mapToResponse(booking);
    }

    // CANCEL BOOKING - User can cancel their own booking
    public BookingResponse cancelBooking(String bookingId, String userId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Only allow user to cancel their own booking (unless admin)
        if (!isAdmin && !booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied. You can only cancel your own bookings.");
        }

        // Check if already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        // Cancel the booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        Booking updatedBooking = bookingRepository.save(booking);

        BookingResponse response = mapToResponse(updatedBooking);
        response.setMessage("Booking cancelled successfully. Room is now available.");
        return response;
    }

    // DELETE BOOKING - Admin only
    public String deleteBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        bookingRepository.delete(booking);
        return "Booking deleted successfully.";
    }

    // Helper method
    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .roomId(booking.getRoomId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}


