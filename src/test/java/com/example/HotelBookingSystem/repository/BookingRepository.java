package com.example.HotelBookingSystem.repository;
import com.example.HotelBookingSystem.model.Booking;
import com.example.HotelBookingSystem.model.BookingStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    private Booking booking1;
    private Booking booking2;

    @BeforeEach
    void setUp() {
        booking1 = Booking.builder()
                .userId("user@test.com")
                .hotelId("hotel123")
                .roomId("room123")
                .checkInDate(LocalDate.of(2026, 7, 15))
                .checkOutDate(LocalDate.of(2026, 7, 18))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        booking2 = Booking.builder()
                .userId("user@test.com")
                .hotelId("hotel123")
                .roomId("room456")
                .checkInDate(LocalDate.of(2026, 8, 1))
                .checkOutDate(LocalDate.of(2026, 8, 5))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        bookingRepository.save(booking1);
        bookingRepository.save(booking2);
    }

    @AfterEach
    void tearDown() {
        bookingRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find bookings by user ID")
    void findByUserId() {
        List<Booking> bookings = bookingRepository.findByUserId("user@test.com");

        assertThat(bookings).hasSize(2);
    }

    @Test
    @DisplayName("Should find bookings by hotel ID")
    void findByHotelId() {
        List<Booking> bookings = bookingRepository.findByHotelId("hotel123");

        assertThat(bookings).hasSize(2);
    }

    @Test
    @DisplayName("Should find conflicting bookings - overlap exists")
    void findConflictingBookings_Overlap() {
        // Search for room123 between July 16-17 (overlaps with booking1: July 15-18)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                "room123",
                LocalDate.of(2026, 7, 16),
                LocalDate.of(2026, 7, 17)
        );

        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getRoomId()).isEqualTo("room123");
    }

    @Test
    @DisplayName("Should find no conflicts when dates don't overlap")
    void findConflictingBookings_NoOverlap() {
        // Search for room123 between July 20-25 (no overlap with July 15-18)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                "room123",
                LocalDate.of(2026, 7, 20),
                LocalDate.of(2026, 7, 25)
        );

        assertThat(conflicts).isEmpty();
    }

    @Test
    @DisplayName("Should find no conflicts for different room")
    void findConflictingBookings_DifferentRoom() {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                "room999",
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 7, 18)
        );

        assertThat(conflicts).isEmpty();
    }

    @Test
    @DisplayName("Should not count cancelled bookings as conflicts")
    void findConflictingBookings_CancelledNotCounted() {
        booking1.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking1);

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                "room123",
                LocalDate.of(2026, 7, 16),
                LocalDate.of(2026, 7, 17)
        );

        assertThat(conflicts).isEmpty();
    }

    @Test
    @DisplayName("Should find bookings by room ID and status")
    void findByRoomIdAndStatus() {
        List<Booking> bookings = bookingRepository.findByRoomIdAndStatus("room123", BookingStatus.CONFIRMED);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }
}

