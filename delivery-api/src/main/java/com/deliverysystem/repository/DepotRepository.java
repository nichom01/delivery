package com.deliverysystem.repository;

import com.deliverysystem.domain.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepotRepository extends JpaRepository<Depot, String> {
    Optional<Depot> findByName(String name);
}
