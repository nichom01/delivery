package com.deliverysystem.dto;

import jakarta.validation.constraints.NotBlank;

public class RerouteOrderRequest {

    @NotBlank(message = "routeId is required")
    private String routeId;

    @NotBlank(message = "reason is required")
    private String reason;

    public RerouteOrderRequest() {
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
