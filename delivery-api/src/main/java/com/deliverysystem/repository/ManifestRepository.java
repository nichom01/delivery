package com.deliverysystem.repository;

import com.deliverysystem.domain.Manifest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManifestRepository extends JpaRepository<Manifest, String> {
    Optional<Manifest> findByRouteIdAndDate(String routeId, LocalDate date);
    List<Manifest> findByRouteDepotId(String depotId);
    List<Manifest> findByRouteDepotIdAndDate(String depotId, LocalDate date);
    List<Manifest> findByDriverIdAndDate(String driverId, LocalDate date);
    
    @Query("SELECT m FROM Manifest m WHERE m.route.id = :routeId AND m.date = :date")
    List<Manifest> findManifestsByRouteIdAndDate(@Param("routeId") String routeId, @Param("date") LocalDate date);
}
