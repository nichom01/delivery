package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "drivers")
public class Driver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    private String contact; // Phone/email
    
    @Column(name = "licence_number")
    private String licenceNumber;
    
    @Column(name = "licence_expiry")
    private LocalDate licenceExpiry;
    
    private String shiftInfo; // Shift/availability information
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;
    
    @Column(nullable = false)
    private String status = "ACTIVE";
    
    public Driver() {
    }
    
    public Driver(String id, String name, String contact, String licenceNumber, LocalDate licenceExpiry, String shiftInfo, Depot depot, String status) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.licenceNumber = licenceNumber;
        this.licenceExpiry = licenceExpiry;
        this.shiftInfo = shiftInfo;
        this.depot = depot;
        this.status = status;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public LocalDate getLicenceExpiry() {
        return licenceExpiry;
    }
    
    public void setLicenceExpiry(LocalDate licenceExpiry) {
        this.licenceExpiry = licenceExpiry;
    }
    
    public String getShiftInfo() {
        return shiftInfo;
    }
    
    public void setShiftInfo(String shiftInfo) {
        this.shiftInfo = shiftInfo;
    }
    
    public Depot getDepot() {
        return depot;
    }
    
    public void setDepot(Depot depot) {
        this.depot = depot;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
