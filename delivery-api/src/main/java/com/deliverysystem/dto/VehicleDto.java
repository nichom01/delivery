package com.deliverysystem.dto;

public class VehicleDto {
    private String id;
    private String depotId;
    private String registration;
    private String makeModel;
    private String capacity;
    private String motDue;
    private String nextService;
    private String status;
    
    public VehicleDto() {
    }
    
    public VehicleDto(String id, String depotId, String registration, String makeModel, String capacity, String motDue, String nextService, String status) {
        this.id = id;
        this.depotId = depotId;
        this.registration = registration;
        this.makeModel = makeModel;
        this.capacity = capacity;
        this.motDue = motDue;
        this.nextService = nextService;
        this.status = status;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDepotId() {
        return depotId;
    }
    
    public void setDepotId(String depotId) {
        this.depotId = depotId;
    }
    
    public String getRegistration() {
        return registration;
    }
    
    public void setRegistration(String registration) {
        this.registration = registration;
    }
    
    public String getMakeModel() {
        return makeModel;
    }
    
    public void setMakeModel(String makeModel) {
        this.makeModel = makeModel;
    }
    
    public String getCapacity() {
        return capacity;
    }
    
    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
    
    public String getMotDue() {
        return motDue;
    }
    
    public void setMotDue(String motDue) {
        this.motDue = motDue;
    }
    
    public String getNextService() {
        return nextService;
    }
    
    public void setNextService(String nextService) {
        this.nextService = nextService;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
