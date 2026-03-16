package com.deliverysystem.dto;

import jakarta.validation.constraints.NotBlank;

public class FlagExceptionRequest {
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    public FlagExceptionRequest() {
    }
    
    public FlagExceptionRequest(String reason) {
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
