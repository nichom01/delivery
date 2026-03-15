package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String registration;
    
    private String make;
    
    private String model;
    
    private String capacity; // Weight and/or volume
    
    @Column(name = "mot_date")
    private LocalDate motDate;
    
    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;
    
    @Column(name = "next_service_due")
    private LocalDate nextServiceDue;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;
    
    @Column(nullable = false)
    private String status = "ACTIVE";
    
    public Vehicle() {
    }
    
    public Vehicle(String id, String registration, String make, String model, String capacity, LocalDate motDate, LocalDate lastServiceDate, LocalDate nextServiceDue, Depot depot, String status) {
        this.id = id;
        this.registration = registration;
        this.make = make;
        this.model = model;
        this.capacity = capacity;
        this.motDate = motDate;
        this.lastServiceDate = lastServiceDate;
        this.nextServiceDue = nextServiceDue;
        this.depot = depot;
        this.status = status;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public LocalDate getMotDate() {
        return motDate;
    }
    
    public void setMotDate(LocalDate motDate) {
        this.motDate = motDate;
    }
    
    public LocalDate getLastServiceDate() {
        return lastServiceDate;
    }
    
    public void setLastServiceDate(LocalDate lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }
    
    public LocalDate getNextServiceDue() {
        return nextServiceDue;
    }
    
    public void setNextServiceDue(LocalDate nextServiceDue) {
        this.nextServiceDue = nextServiceDue;
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
