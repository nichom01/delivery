package com.deliverysystem.dto;

public class RouteDto {
    private String id;
    private String depotId;
    private String code;
    private String name;
    private String coverage;
    private Integer postcodeRulesCount;
    private String status;
    
    public RouteDto() {
    }
    
    public RouteDto(String id, String depotId, String code, String name, String coverage, Integer postcodeRulesCount, String status) {
        this.id = id;
        this.depotId = depotId;
        this.code = code;
        this.name = name;
        this.coverage = coverage;
        this.postcodeRulesCount = postcodeRulesCount;
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
    
    public String getCoverage() {
        return coverage;
    }
    
    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }
    
    public Integer getPostcodeRulesCount() {
        return postcodeRulesCount;
    }
    
    public void setPostcodeRulesCount(Integer postcodeRulesCount) {
        this.postcodeRulesCount = postcodeRulesCount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
