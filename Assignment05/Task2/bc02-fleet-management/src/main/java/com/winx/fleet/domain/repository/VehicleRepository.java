package com.winx.fleet.domain.repository;

import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByProviderId(Long providerId);

    List<Vehicle> findByStatus(VehicleStatus status);
}
