package com.deliverysystem.service;

import com.deliverysystem.domain.DriverLocationSample;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.DriverLocationPointRequest;
import com.deliverysystem.dto.DriverLocationSampleDto;
import com.deliverysystem.repository.DriverLocationSampleRepository;
import com.deliverysystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverLocationService {

    private static final int MAX_RECORDED_AGE_DAYS = 30;
    private static final int FUTURE_SKEW_MINUTES = 5;

    private final DriverLocationSampleRepository repository;
    private final UserRepository userRepository;
    private final Clock clock;

    public DriverLocationService(
            DriverLocationSampleRepository repository,
            UserRepository userRepository,
            @Autowired(required = false) Clock clock) {
        this.repository = repository;
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    public List<DriverLocationSampleDto> listSamples(User requester, String userId, LocalDate dayUtc) {
        User target = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (target.getRole() != User.UserRole.DRIVER) {
            throw new IllegalArgumentException("Location history is only available for driver app users");
        }
        authorizeLocationRead(requester, target);

        Instant start = dayUtc.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = dayUtc.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return repository
            .findByUser_IdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(userId, start, end)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private void authorizeLocationRead(User requester, User target) {
        switch (requester.getRole()) {
            case DRIVER:
                if (!requester.getId().equals(target.getId())) {
                    throw new AccessDeniedException("You may only view your own location history");
                }
                break;
            case DEPOT_MANAGER:
                if (requester.getDepotId() == null || !requester.getDepotId().equals(target.getDepotId())) {
                    throw new AccessDeniedException("You may only view drivers in your depot");
                }
                break;
            case CENTRAL_ADMIN:
                break;
            default:
                throw new AccessDeniedException("Access denied");
        }
    }

    private DriverLocationSampleDto toDto(DriverLocationSample entity) {
        DriverLocationSampleDto dto = new DriverLocationSampleDto();
        dto.setId(entity.getId());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setRecordedAt(entity.getRecordedAt());
        dto.setReceivedAt(entity.getReceivedAt());
        return dto;
    }
}
