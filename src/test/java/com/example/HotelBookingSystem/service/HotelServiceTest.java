package com.example.HotelBookingSystem.service;
import com.example.HotelBookingSystem.DTO.HotelRequest;
import com.example.HotelBookingSystem.DTO.HotelResponse;
import com.example.HotelBookingSystem.model.Hotel;
import com.example.HotelBookingSystem.repository.Hotelrepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private Hotelrepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel hotel;
    private HotelRequest hotelRequest;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id("hotel123")
                .name("Taj Palace")
                .city("Mumbai")
                .address("Colaba, Mumbai")
                .description("5-star luxury hotel")
                .rating(4.8)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        hotelRequest = new HotelRequest();
        hotelRequest.setName("Taj Palace");
        hotelRequest.setCity("Mumbai");
        hotelRequest.setAddress("Colaba, Mumbai");
        hotelRequest.setDescription("5-star luxury hotel");
        hotelRequest.setRating(4.8);
    }

    @Test
    @DisplayName("Should create hotel successfully")
    void createHotel_Success() {
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        HotelResponse response = hotelService.createHotel(hotelRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Taj Palace");
        assertThat(response.getCity()).isEqualTo("Mumbai");
        assertThat(response.getRating()).isEqualTo(4.8);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    @DisplayName("Should get all hotels")
    void getAllHotels_Success() {
        Hotel hotel2 = Hotel.builder()
                .id("hotel456")
                .name("Oberoi")
                .city("Delhi")
                .rating(4.5)
                .build();

        when(hotelRepository.findAll()).thenReturn(Arrays.asList(hotel, hotel2));

        List<HotelResponse> hotels = hotelService.getAllHotels();

        assertThat(hotels).hasSize(2);
        assertThat(hotels.get(0).getName()).isEqualTo("Taj Palace");
        assertThat(hotels.get(1).getName()).isEqualTo("Oberoi");
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get hotel by ID")
    void getHotelById_Success() {
        when(hotelRepository.findById("hotel123")).thenReturn(Optional.of(hotel));

        HotelResponse response = hotelService.getHotelById("hotel123");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("hotel123");
        assertThat(response.getName()).isEqualTo("Taj Palace");
    }

    @Test
    @DisplayName("Should throw exception when hotel not found")
    void getHotelById_NotFound() {
        when(hotelRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelById("invalid"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should update hotel successfully")
    void updateHotel_Success() {
        when(hotelRepository.findById("hotel123")).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        hotelRequest.setName("Taj Palace Updated");
        HotelResponse response = hotelService.updateHotel("hotel123", hotelRequest);

        assertThat(response).isNotNull();
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    @DisplayName("Should delete hotel successfully")
    void deleteHotel_Success() {
        when(hotelRepository.findById("hotel123")).thenReturn(Optional.of(hotel));
        doNothing().when(hotelRepository).delete(hotel);

        String result = hotelService.deleteHotel("hotel123");

        assertThat(result).contains("deleted");
        verify(hotelRepository, times(1)).delete(hotel);
    }

    @Test
    @DisplayName("Should get hotels by city")
    void getHotelsByCity_Success() {
        when(hotelRepository.findByCityIgnoreCase("Mumbai")).thenReturn(List.of(hotel));

        List<HotelResponse> hotels = hotelService.getHotelsByCity("Mumbai");

        assertThat(hotels).hasSize(1);
        assertThat(hotels.get(0).getCity()).isEqualTo("Mumbai");
    }
}

