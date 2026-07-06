package com.winx.booking.domain.repository;

import com.winx.booking.domain.model.Booking;
import com.winx.booking.domain.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    @Query("select b from Booking b where b.vehicleSnapshot.vehicleId = :vehicleId and b.status = :status")
    List<Booking> findByVehicleIdAndStatus(@Param("vehicleId") Long vehicleId, @Param("status") BookingStatus status);

    @Query("select b from Booking b where b.vehicleSnapshot.vehicleId = :vehicleId")
    List<Booking> findByVehicleId(@Param("vehicleId") Long vehicleId);

    List<Booking> findByUserIdOrderByStartTimeDesc(Long userId);
}
