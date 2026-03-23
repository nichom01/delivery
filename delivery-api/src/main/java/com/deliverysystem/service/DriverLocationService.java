package com.deliverysystem.service;

import com.deliverysystem.domain.DriverLocationSample;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.DriverLocationPointRequest;
import com.deliverysystem.repository.DriverLocationSampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverLocationService {

    private static final int MAX_RECORDED_AGE_DAYS = 30;
    private static final int FUTURE_SKEW_MINUTES = 5;

    private final DriverLocationSampleRepository repository;
    private final Clock clock;

    public DriverLocationService(
            DriverLocationSampleRepository repository,
            @Autowired(required = false) Clock clock) {
        this.repository = repository;
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    @Transactional
    public int saveSamples(User user, List<DriverLocationPointRequest> points) {
        Instant now = clock.instant();
        Instant oldestAllowed = now.minus(MAX_RECORDED_AGE_DAYS, ChronoUnit.DAYS);
        Instant newestAllowed = now.plus(FUTURE_SKEW_MINUTES, ChronoUnit.MINUTES);

        List<DriverLocationSample> entities = new ArrayList<>(points.size());
        for (DriverLocationPointRequest p : points) {
            Instant recorded = p.getRecordedAt();
            if (recorded.isBefore(oldestAllowed)) {
                throw new IllegalArgumentException(
                    "recordedAt must not be older than " + MAX_RECORDED_AGE_DAYS + " days");
            }
            if (recorded.isAfter(newestAllowed)) {
                throw new IllegalArgumentException(
                    "recordedAt must not be more than " + FUTURE_SKEW_MINUTES + " minutes in the future");
            }
            DriverLocationSample row = new DriverLocationSample();
            row.setUser(user);
            row.setLatitude(p.getLatitude());
            row.setLongitude(p.getLongitude());
            row.setRecordedAt(recorded);
            row.setReceivedAt(now);
            entities.add(row);
        }
        repository.saveAll(entities);
        return entities.size();
    }
}
