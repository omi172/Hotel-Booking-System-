package com.example.HotelBookingSystem.repository;
import com.example.HotelBookingSystem.model.Hotel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class HotelRepositoryTest {

    @Autowired
    private Hotelrepository hotelRepository;

    private Hotel hotel1;
    private Hotel hotel2;

    @BeforeEach
    void setUp() {
        hotel1 = Hotel.builder()
                .name("Taj Palace")
                .city("Mumbai")
                .address("Colaba, Mumbai")
                .description("5-star luxury hotel")
                .rating(4.8)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        hotel2 = Hotel.builder()
                .name("Oberoi Grand")
                .city("Mumbai")
                .address("Marine Drive, Mumbai")
                .description("Heritage luxury hotel")
                .rating(4.6)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        hotelRepository.save(hotel1);
        hotelRepository.save(hotel2);
    }

    @AfterEach
    void tearDown() {
        hotelRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save hotel")
    void saveHotel() {
        Hotel newHotel = Hotel.builder()
                .name("ITC Maurya")
                .city("Delhi")
                .rating(4.7)
                .build();

        Hotel saved = hotelRepository.save(newHotel);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("ITC Maurya");
    }

    @Test
    @DisplayName("Should find hotel by ID")
    void findById() {
        Optional<Hotel> found = hotelRepository.findById(hotel1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Taj Palace");
    }

    @Test
    @DisplayName("Should find hotels by city (case insensitive)")
    void findByCityIgnoreCase() {
        List<Hotel> hotels = hotelRepository.findByCityIgnoreCase("mumbai");

        assertThat(hotels).hasSize(2);
        assertThat(hotels).extracting(Hotel::getCity).containsOnly("Mumbai");
    }

    @Test
    @DisplayName("Should find all hotels")
    void findAll() {
        List<Hotel> hotels = hotelRepository.findAll();

        assertThat(hotels).hasSize(2);
    }

    @Test
    @DisplayName("Should update hotel")
    void updateHotel() {
        hotel1.setRating(4.9);
        Hotel updated = hotelRepository.save(hotel1);

        assertThat(updated.getRating()).isEqualTo(4.9);
    }

    @Test
    @DisplayName("Should delete hotel")
    void deleteHotel() {
        hotelRepository.delete(hotel1);

        Optional<Hotel> found = hotelRepository.findById(hotel1.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for non-existent city")
    void findByCityIgnoreCase_NotFound() {
        List<Hotel> hotels = hotelRepository.findByCityIgnoreCase("Kolkata");

        assertThat(hotels).isEmpty();
    }
}

