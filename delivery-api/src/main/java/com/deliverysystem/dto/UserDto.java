package com.deliverysystem.dto;

public class UserDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private String depotId;
    private String lastLogin;
    private String status;
    
    public UserDto() {
    }
    
    public UserDto(String id, String name, String email, String role, String depotId, String lastLogin, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.depotId = depotId;
        this.lastLogin = lastLogin;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getDepotId() {
        return depotId;
    }
    
    public void setDepotId(String depotId) {
        this.depotId = depotId;
    }
    
    public String getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
