package com.example.HotelBookingSystem.repository;
import com.example.HotelBookingSystem.model.Payment;
import com.example.HotelBookingSystem.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByUserId(String userId);

    List<Payment> findByBookingId(String bookingId);

    Optional<Payment> findByBookingIdAndPaymentStatus(String bookingId, PaymentStatus status);

    List<Payment> findByPaymentStatus(PaymentStatus status);
}


