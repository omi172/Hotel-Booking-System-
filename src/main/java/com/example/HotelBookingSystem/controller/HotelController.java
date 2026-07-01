package com.example.HotelBookingSystem.controller;

import com.example.HotelBookingSystem.DTO.HotelRequest;
import com.example.HotelBookingSystem.DTO.HotelResponse;
import com.example.HotelBookingSystem.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    // ==================== ADMIN ENDPOINTS ====================

    // CREATE - Admin only
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelResponse> createHotel(@Valid @RequestBody HotelRequest request) {
        HotelResponse response = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UPDATE - Admin only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable String id,
            @Valid @RequestBody HotelRequest request) {
        HotelResponse response = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(response);
    }

    // DELETE - Admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteHotel(@PathVariable String id) {
        String message = hotelService.deleteHotel(id);
        return ResponseEntity.ok(message);
    }

    // ==================== USER + ADMIN ENDPOINTS ====================

    // GET ALL - Any authenticated user
    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        List<HotelResponse> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    // GET BY ID - Any authenticated user
    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable String id) {
        HotelResponse response = hotelService.getHotelById(id);
        return ResponseEntity.ok(response);
    }

    // SEARCH BY CITY - Any authenticated user
    @GetMapping("/search/city")
    public ResponseEntity<List<HotelResponse>> getHotelsByCity(@RequestParam String city) {
        List<HotelResponse> hotels = hotelService.getHotelsByCity(city);
        return ResponseEntity.ok(hotels);
    }

    // SEARCH BY NAME - Any authenticated user
    @GetMapping("/search/name")
    public ResponseEntity<List<HotelResponse>> searchHotelsByName(@RequestParam String name) {
        List<HotelResponse> hotels = hotelService.searchHotelsByName(name);
        return ResponseEntity.ok(hotels);
    }
}
