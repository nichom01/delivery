package com.deliverysystem.repository;

import com.deliverysystem.domain.Box;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoxRepository extends JpaRepository<Box, String> {
    List<Box> findByOrderId(String orderId);
    List<Box> findByManifestId(String manifestId);
    List<Box> findByOrderIdAndStatus(String orderId, Box.BoxStatus status);
    List<Box> findByStatus(Box.BoxStatus status);
    
    @Query("SELECT b FROM Box b WHERE b.order.route.id = :routeId AND b.status = :status")
    List<Box> findByOrderRouteIdAndStatus(@Param("routeId") String routeId, @Param("status") Box.BoxStatus status);
    
    @Query("SELECT b FROM Box b WHERE b.order.route.depot.id = :depotId AND b.status = :status")
    List<Box> findByOrderDepotIdAndStatus(@Param("depotId") String depotId, @Param("status") Box.BoxStatus status);
}
