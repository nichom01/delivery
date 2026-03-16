package com.deliverysystem.dto;

public class UpdateDriverRequest {

    private String name;
    private String contact;
    private String licenceNumber;
    private String licenceExpiry;
    private String shiftInfo;
    private String status;

    public UpdateDriverRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    public String getLicenceExpiry() {
        return licenceExpiry;
    }

    public void setLicenceExpiry(String licenceExpiry) {
        this.licenceExpiry = licenceExpiry;
    }

    public String getShiftInfo() {
        return shiftInfo;
    }

    public void setShiftInfo(String shiftInfo) {
        this.shiftInfo = shiftInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
