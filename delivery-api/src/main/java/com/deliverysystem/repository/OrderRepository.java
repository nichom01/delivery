package com.deliverysystem.repository;

import com.deliverysystem.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderIdAndDespatchId(String orderId, String despatchId);
    List<Order> findByRouteId(String routeId);
    List<Order> findByRouteDepotId(String depotId);

    @Query("SELECT o FROM Order o WHERE o.route.depot.id = :depotId")
    List<Order> findByDepotId(@Param("depotId") String depotId);

    @Query("SELECT o FROM Order o WHERE o.route.id = :routeId AND o.requestedDeliveryDate = :date")
    List<Order> findByRouteIdAndRequestedDeliveryDate(
            @Param("routeId") String routeId, @Param("date") LocalDate date);

    @Query("SELECT o FROM Order o WHERE o.route.depot.id = :depotId AND o.requestedDeliveryDate = :date")
    List<Order> findByDepotIdAndRequestedDeliveryDate(
            @Param("depotId") String depotId, @Param("date") LocalDate date);
}
