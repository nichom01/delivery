package com.deliverysystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SubmitDriverLocationsRequest {

    @NotEmpty(message = "must contain at least one location")
    @Size(max = 100, message = "must not contain more than 100 locations")
    @Valid
    private List<DriverLocationPointRequest> locations;

    public List<DriverLocationPointRequest> getLocations() {
        return locations;
    }

    public void setLocations(List<DriverLocationPointRequest> locations) {
        this.locations = locations;
    }
}
