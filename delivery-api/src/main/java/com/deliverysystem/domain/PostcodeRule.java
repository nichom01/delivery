package com.deliverysystem.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "postcode_rules", indexes = {
    @Index(name = "idx_postcode_pattern", columnList = "pattern"),
    @Index(name = "idx_postcode_level", columnList = "level"),
    @Index(name = "idx_postcode_dates", columnList = "effective_from, effective_to")
})
public class PostcodeRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String pattern; // Postcode pattern (full, sector, district, area, or letter)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostcodeLevel level;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
    
    public PostcodeRule() {
    }
    
    public PostcodeRule(String id, String pattern, PostcodeLevel level, Route route, LocalDate effectiveFrom, LocalDate effectiveTo) {
        this.id = id;
        this.pattern = pattern;
        this.level = level;
        this.route = route;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public PostcodeLevel getLevel() {
        return level;
    }
    
    public void setLevel(PostcodeLevel level) {
        this.level = level;
    }
    
    public Route getRoute() {
        return route;
    }
    
    public void setRoute(Route route) {
        this.route = route;
    }
    
    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }
    
    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
    
    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
    
    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
    
    public enum PostcodeLevel {
        FULL,      // SW1A 1AA
        SECTOR,    // SW1A 1
        DISTRICT,  // SW1A
        AREA,      // SW
        LETTER     // S
    }
}
