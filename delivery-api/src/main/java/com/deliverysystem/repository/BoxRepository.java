package com.deliverysystem.repository;

import com.deliverysystem.domain.Box;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoxRepository extends JpaRepository<Box, String> {
    List<Box> findByOrderId(String orderId);
    List<Box> findByManifestId(String manifestId);
    List<Box> findByOrderIdAndStatus(String orderId, Box.BoxStatus status);
}
