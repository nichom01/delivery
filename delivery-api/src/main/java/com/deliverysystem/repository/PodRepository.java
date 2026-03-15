package com.deliverysystem.repository;

import com.deliverysystem.domain.Pod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PodRepository extends JpaRepository<Pod, String> {
    List<Pod> findByManifestId(String manifestId);
    List<Pod> findByOrderId(String orderId);
}
