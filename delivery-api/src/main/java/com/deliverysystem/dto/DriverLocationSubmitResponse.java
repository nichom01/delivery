package com.deliverysystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DriverLocationSubmitResponse {

    @JsonProperty("savedCount")
    private int savedCount;

    public DriverLocationSubmitResponse() {
    }

    public DriverLocationSubmitResponse(int savedCount) {
        this.savedCount = savedCount;
    }

    public int getSavedCount() {
        return savedCount;
    }

    public void setSavedCount(int savedCount) {
        this.savedCount = savedCount;
    }
}
