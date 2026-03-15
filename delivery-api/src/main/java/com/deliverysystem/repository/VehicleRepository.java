package com.deliverysystem.repository;

import com.deliverysystem.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    List<Vehicle> findByDepotId(String depotId);
    Vehicle findByRegistration(String registration);
}
