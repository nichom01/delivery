package com.deliverysystem.repository;

import com.deliverysystem.domain.DriverLocationSample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface DriverLocationSampleRepository extends JpaRepository<DriverLocationSample, String> {

    List<DriverLocationSample> findByUser_IdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
        String userId,
        Instant startInclusive,
        Instant endExclusive);
}
