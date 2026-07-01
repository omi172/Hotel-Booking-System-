package com.example.HotelBookingSystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.HotelBookingSystem.DTO.BookingRequest;
import com.example.HotelBookingSystem.DTO.BookingResponse;
import com.example.HotelBookingSystem.model.BookingStatus;
import com.example.HotelBookingSystem.config.JwtUtils;
import com.example.HotelBookingSystem.config.TestSecurityConfig;
import com.example.HotelBookingSystem.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {com.example.HotelBookingSystem.config.SecurityConfig.class}
        )
)
@ContextConfiguration(classes = {BookingController.class, TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = true)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    private ObjectMapper objectMapper;
    private BookingRequest bookingRequest;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        bookingRequest = new BookingRequest();
        bookingRequest.setHotelId("hotel123");
        bookingRequest.setRoomId("room123");
        bookingRequest.setCheckInDate(LocalDate.of(2026, 7, 15));
        bookingRequest.setCheckOutDate(LocalDate.of(2026, 7, 18));

        bookingResponse = BookingResponse.builder()
                .id("booking123")
                .userId("user@test.com")
                .hotelId("hotel123")
                .roomId("room123")
                .checkInDate(LocalDate.of(2026, 7, 15))
                .checkOutDate(LocalDate.of(2026, 7, 18))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .message("Booking confirmed successfully!")
                .build();
    }

    @Test
    @DisplayName("Should create booking - 201")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void createBooking_Success() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class), anyString()))
                .thenReturn(bookingResponse);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.hotelId").value("hotel123"));
    }

    @Test
    @DisplayName("Should get my bookings - 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getMyBookings_Success() throws Exception {
        when(bookingService.getMyBookings(anyString())).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/api/bookings/my"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user@test.com"));
    }

    @Test
    @DisplayName("Should cancel booking - 200")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void cancelBooking_Success() throws Exception {
        BookingResponse cancelledResponse = BookingResponse.builder()
                .id("booking123")
                .userId("user@test.com")
                .hotelId("hotel123")
                .roomId("room123")
                .status(BookingStatus.CANCELLED)
                .message("Booking cancelled successfully!")
                .build();

        when(bookingService.cancelBooking(anyString(), anyString(), anyBoolean()))
                .thenReturn(cancelledResponse);

        mockMvc.perform(put("/api/bookings/booking123/cancel"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Admin should get all bookings - 200")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void getAllBookings_Admin_Success() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/api/bookings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("booking123"));
    }

    @Test
    @DisplayName("User should NOT get all bookings - 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getAllBookings_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated should get 401")
    @WithAnonymousUser
    void createBooking_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}

