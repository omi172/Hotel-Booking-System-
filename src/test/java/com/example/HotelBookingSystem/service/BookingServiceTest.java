package com.example.HotelBookingSystem.service;
import com.example.HotelBookingSystem.DTO.BookingRequest;
import com.example.HotelBookingSystem.DTO.BookingResponse;
import com.example.HotelBookingSystem.model.Booking;
import com.example.HotelBookingSystem.model.BookingStatus;
import com.example.HotelBookingSystem.model.Hotel;
import com.example.HotelBookingSystem.repository.BookingRepository;
import com.example.HotelBookingSystem.repository.Hotelrepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private Hotelrepository hotelRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;
    private BookingRequest bookingRequest;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id("hotel123")
                .name("Taj Palace")
                .city("Mumbai")
                .build();

        booking = Booking.builder()
                .id("booking123")
                .userId("user@test.com")
                .hotelId("hotel123")
                .roomId("room123")
                .checkInDate(LocalDate.of(2026, 7, 15))
                .checkOutDate(LocalDate.of(2026, 7, 18))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        bookingRequest = new BookingRequest();
        bookingRequest.setHotelId("hotel123");
        bookingRequest.setRoomId("room123");
        bookingRequest.setCheckInDate(LocalDate.of(2026, 7, 15));
        bookingRequest.setCheckOutDate(LocalDate.of(2026, 7, 18));
    }

    @Test
    @DisplayName("Should create booking successfully")
    void createBooking_Success() {
        when(hotelRepository.findById("hotel123")).thenReturn(Optional.of(hotel));
        when(bookingRepository.findConflictingBookings(any(), any(), any())).thenReturn(new ArrayList<>());
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.createBooking(bookingRequest, "user@test.com");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.getHotelId()).isEqualTo("hotel123");
        assertThat(response.getRoomId()).isEqualTo("room123");
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when dates conflict")
    void createBooking_DateConflict() {
        when(hotelRepository.findById("hotel123")).thenReturn(Optional.of(hotel));
        when(bookingRepository.findConflictingBookings(any(), any(), any())).thenReturn(List.of(booking));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @DisplayName("Should throw exception when check-out before check-in")
    void createBooking_InvalidDates() {
        bookingRequest.setCheckInDate(LocalDate.of(2026, 7, 18));
        bookingRequest.setCheckOutDate(LocalDate.of(2026, 7, 15));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Check-out date must be after check-in date");
    }

    @Test
    @DisplayName("Should throw exception when hotel not found")
    void createBooking_HotelNotFound() {
        bookingRequest.setCheckInDate(LocalDate.of(2026, 7, 15));
        bookingRequest.setCheckOutDate(LocalDate.of(2026, 7, 18));
        when(hotelRepository.findById("hotel123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Hotel not found");
    }

    @Test
    @DisplayName("Should get user bookings")
    void getMyBookings_Success() {
        when(bookingRepository.findByUserId("user@test.com")).thenReturn(List.of(booking));

        List<BookingResponse> bookings = bookingService.getMyBookings("user@test.com");

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getUserId()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Should cancel booking successfully")
    void cancelBooking_Success() {
        when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

        Booking cancelledBooking = Booking.builder()
                .id("booking123")
                .userId("user@test.com")
                .status(BookingStatus.CANCELLED)
                .build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(cancelledBooking);

        BookingResponse response = bookingService.cancelBooking("booking123", "user@test.com", false);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling other user's booking")
    void cancelBooking_AccessDenied() {
        when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking("booking123", "other@test.com", false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("Should throw exception when booking already cancelled")
    void cancelBooking_AlreadyCancelled() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking("booking123", "user@test.com", false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    @DisplayName("Admin should be able to cancel any booking")
    void cancelBooking_AdminSuccess() {
        when(bookingRepository.findById("booking123")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.cancelBooking("booking123", "admin@test.com", true);

        assertThat(response).isNotNull();
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }
}

