package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostcodeRule> postcodeRules = new ArrayList<>();
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
    
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Manifest> manifests = new ArrayList<>();
    
    public Route() {
    }
    
    public Route(String id, String code, String name, String description, Depot depot, List<PostcodeRule> postcodeRules, List<Order> orders, List<Manifest> manifests) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.depot = depot;
        this.postcodeRules = postcodeRules;
        this.orders = orders;
        this.manifests = manifests;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Depot getDepot() {
        return depot;
    }
    
    public void setDepot(Depot depot) {
        this.depot = depot;
    }
    
    public List<PostcodeRule> getPostcodeRules() {
        return postcodeRules;
    }
    
    public void setPostcodeRules(List<PostcodeRule> postcodeRules) {
        this.postcodeRules = postcodeRules;
    }
    
    public List<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
    public List<Manifest> getManifests() {
        return manifests;
    }
    
    public void setManifests(List<Manifest> manifests) {
        this.manifests = manifests;
    }
}
