package com.deliverysystem.dto;

public class CurrentUserDto {
    private String name;
    private String role;
    private String initials;
    private String depotId;
    
    public CurrentUserDto() {
    }
    
    public CurrentUserDto(String name, String role, String initials, String depotId) {
        this.name = name;
        this.role = role;
        this.initials = initials;
        this.depotId = depotId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getInitials() {
        return initials;
    }
    
    public void setInitials(String initials) {
        this.initials = initials;
    }
    
    public String getDepotId() {
        return depotId;
    }
    
    public void setDepotId(String depotId) {
        this.depotId = depotId;
    }
}
