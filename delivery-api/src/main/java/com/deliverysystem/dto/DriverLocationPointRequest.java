package com.deliverysystem.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class DriverLocationPointRequest {

    @NotNull(message = "is required")
    @DecimalMin(value = "-90.0", inclusive = true, message = "must be between -90 and 90")
    @DecimalMax(value = "90.0", inclusive = true, message = "must be between -90 and 90")
    private BigDecimal latitude;

    @NotNull(message = "is required")
    @DecimalMin(value = "-180.0", inclusive = true, message = "must be between -180 and 180")
    @DecimalMax(value = "180.0", inclusive = true, message = "must be between -180 and 180")
    private BigDecimal longitude;

    @NotNull(message = "is required")
    private Instant recordedAt;

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }
}
