package com.deliverysystem.dto;

public class DriverDto {
    private String id;
    private String depotId;
    private String name;
    private String licenceNo;
    private String expiry;
    private String contact;
    private String todaysRouteId;
    private String status;
    
    public DriverDto() {
    }
    
    public DriverDto(String id, String depotId, String name, String licenceNo, String expiry, String contact, String todaysRouteId, String status) {
        this.id = id;
        this.depotId = depotId;
        this.name = name;
        this.licenceNo = licenceNo;
        this.expiry = expiry;
        this.contact = contact;
        this.todaysRouteId = todaysRouteId;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLicenceNo() {
        return licenceNo;
    }
    
    public void setLicenceNo(String licenceNo) {
        this.licenceNo = licenceNo;
    }
    
    public String getExpiry() {
        return expiry;
    }
    
    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public String getTodaysRouteId() {
        return todaysRouteId;
    }
    
    public void setTodaysRouteId(String todaysRouteId) {
        this.todaysRouteId = todaysRouteId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
