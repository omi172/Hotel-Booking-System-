package com.example.HotelBookingSystem.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.HotelBookingSystem.DTO.HotelRequest;
import com.example.HotelBookingSystem.DTO.HotelResponse;
import com.example.HotelBookingSystem.config.JwtUtils;
import com.example.HotelBookingSystem.config.TestSecurityConfig;
import com.example.HotelBookingSystem.service.HotelService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HotelController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {com.example.HotelBookingSystem.config.SecurityConfig.class}
        )
)
@ContextConfiguration(classes = {HotelController.class, TestSecurityConfig.class})
@AutoConfigureMockMvc(addFilters = true)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private HotelRequest hotelRequest;
    private HotelResponse hotelResponse;

    @BeforeEach
    void setUp() {
        hotelRequest = new HotelRequest();
        hotelRequest.setName("Taj Palace");
        hotelRequest.setCity("Mumbai");
        hotelRequest.setAddress("Colaba, Mumbai");
        hotelRequest.setDescription("5-star luxury hotel");
        hotelRequest.setRating(4.8);

        hotelResponse = HotelResponse.builder()
                .id("hotel123")
                .name("Taj Palace")
                .city("Mumbai")
                .address("Colaba, Mumbai")
                .description("5-star luxury hotel")
                .rating(4.8)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Admin should create hotel - 201")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void createHotel_Admin_Success() throws Exception {
        when(hotelService.createHotel(any(HotelRequest.class))).thenReturn(hotelResponse);

        mockMvc.perform(post("/api/hotels/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hotelRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Taj Palace"))
                .andExpect(jsonPath("$.city").value("Mumbai"))
                .andExpect(jsonPath("$.rating").value(4.8));
    }

    @Test
    @DisplayName("User should NOT create hotel - 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void createHotel_User_Forbidden() throws Exception {
        mockMvc.perform(post("/api/hotels/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hotelRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get all hotels - 200")
    @WithMockUser(roles = "USER")
    void getAllHotels_Success() throws Exception {
        HotelResponse hotel2 = HotelResponse.builder()
                .id("hotel456")
                .name("Oberoi Grand")
                .city("Delhi")
                .rating(4.5)
                .build();

        when(hotelService.getAllHotels()).thenReturn(Arrays.asList(hotelResponse, hotel2));

        mockMvc.perform(get("/api/hotels"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Taj Palace"))
                .andExpect(jsonPath("$[1].name").value("Oberoi Grand"));
    }

    @Test
    @DisplayName("Should get hotel by ID - 200")
    @WithMockUser(roles = "USER")
    void getHotelById_Success() throws Exception {
        when(hotelService.getHotelById("hotel123")).thenReturn(hotelResponse);

        mockMvc.perform(get("/api/hotels/hotel123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("hotel123"))
                .andExpect(jsonPath("$.name").value("Taj Palace"));
    }

    @Test
    @DisplayName("Admin should update hotel - 200")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void updateHotel_Admin_Success() throws Exception {
        HotelResponse updatedResponse = HotelResponse.builder()
                .id("hotel123")
                .name("Taj Palace Updated")
                .city("Mumbai")
                .rating(4.9)
                .build();

        when(hotelService.updateHotel(anyString(), any(HotelRequest.class))).thenReturn(updatedResponse);

        hotelRequest.setName("Taj Palace Updated");
        mockMvc.perform(put("/api/hotels/hotel123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hotelRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Taj Palace Updated"));
    }

    @Test
    @DisplayName("User should NOT update hotel - 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void updateHotel_User_Forbidden() throws Exception {
        mockMvc.perform(put("/api/hotels/hotel123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hotelRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should delete hotel - 200")
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deleteHotel_Admin_Success() throws Exception {
        when(hotelService.deleteHotel("hotel123")).thenReturn("Hotel deleted successfully");

        mockMvc.perform(delete("/api/hotels/hotel123"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User should NOT delete hotel - 403")
    @WithMockUser(username = "user@test.com", roles = "USER")
    void deleteHotel_User_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/hotels/hotel123"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should search hotels by city - 200")
    @WithMockUser(roles = "USER")
    void getHotelsByCity_Success() throws Exception {
        when(hotelService.getHotelsByCity("Mumbai")).thenReturn(List.of(hotelResponse));

        mockMvc.perform(get("/api/hotels/search/city").param("city", "Mumbai"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Mumbai"));
    }

    @Test
    @DisplayName("Unauthenticated should get 401")
    @WithAnonymousUser
    void getHotels_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/hotels/"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}



