package com.example.HotelBookingSystem.repository;

import com.example.HotelBookingSystem.model.Booking;
import com.example.HotelBookingSystem.model.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByUserId(String userId);

    List<Booking> findByHotelId(String hotelId);

    List<Booking> findByRoomIdAndStatus(String roomId, BookingStatus status);

    // Check for date conflicts: find bookings where dates overlap
    @Query("{ 'roomId': ?0, 'status': 'CONFIRMED', " +
            "'checkInDate': { $lt: ?2 }, 'checkOutDate': { $gt: ?1 } }")
    List<Booking> findConflictingBookings(String roomId, LocalDate checkIn, LocalDate checkOut);
}



