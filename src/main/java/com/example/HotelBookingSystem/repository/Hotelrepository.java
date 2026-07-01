package com.example.HotelBookingSystem.repository;

import com.example.HotelBookingSystem.model.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Hotelrepository extends MongoRepository<Hotel, String> {

    List<Hotel> findByCity(String city);

    List<Hotel> findByRatingGreaterThanEqual(Double rating);

    List<Hotel> findByCityIgnoreCase(String city);

    List<Hotel> findByNameContainingIgnoreCase(String name);
}
