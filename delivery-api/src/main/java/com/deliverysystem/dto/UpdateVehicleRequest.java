package com.deliverysystem.dto;

public class UpdateVehicleRequest {

    private String registration;
    private String make;
    private String model;
    private String capacity;
    private String motDate;
    private String nextServiceDue;
    private String status;

    public UpdateVehicleRequest() {
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getMotDate() {
        return motDate;
    }

    public void setMotDate(String motDate) {
        this.motDate = motDate;
    }

    public String getNextServiceDue() {
        return nextServiceDue;
    }

    public void setNextServiceDue(String nextServiceDue) {
        this.nextServiceDue = nextServiceDue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
