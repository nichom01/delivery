package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "manifests", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"route_id", "date"})
})
public class Manifest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ManifestStatus status = ManifestStatus.DRAFT;
    
    @OneToMany(mappedBy = "manifest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Box> boxes = new ArrayList<>();
    
    public Manifest() {
    }
    
    public Manifest(String id, Route route, LocalDate date, Vehicle vehicle, Driver driver, ManifestStatus status, List<Box> boxes) {
        this.id = id;
        this.route = route;
        this.date = date;
        this.vehicle = vehicle;
        this.driver = driver;
        this.status = status;
        this.boxes = boxes;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Route getRoute() {
        return route;
    }
    
    public void setRoute(Route route) {
        this.route = route;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    
    public Driver getDriver() {
        return driver;
    }
    
    public void setDriver(Driver driver) {
        this.driver = driver;
    }
    
    public ManifestStatus getStatus() {
        return status;
    }
    
    public void setStatus(ManifestStatus status) {
        this.status = status;
    }
    
    public List<Box> getBoxes() {
        return boxes;
    }
    
    public void setBoxes(List<Box> boxes) {
        this.boxes = boxes;
    }
    
    public enum ManifestStatus {
        DRAFT,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETE
    }
}
