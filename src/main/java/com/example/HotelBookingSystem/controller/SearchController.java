package com.example.HotelBookingSystem.controller;

import com.example.HotelBookingSystem.DTO.SearchRequest;
import com.example.HotelBookingSystem.DTO.SearchResponse;
import com.example.HotelBookingSystem.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // Search with request body
    @PostMapping
    public ResponseEntity<List<SearchResponse>> searchHotels(@Valid @RequestBody SearchRequest request) {
        List<SearchResponse> results = searchService.searchAvailableHotels(
                request.getCity(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        return ResponseEntity.ok(results);
    }

    // Search with query parameters (alternative)
    @GetMapping
    public ResponseEntity<List<SearchResponse>> searchHotelsGet(
            @RequestParam String city,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut) {
        List<SearchResponse> results = searchService.searchAvailableHotels(city, checkIn, checkOut);
        return ResponseEntity.ok(results);
    }
}


