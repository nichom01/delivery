package com.deliverysystem.dto;

public class LoginResponse {
    private String token;
    private CurrentUserDto user;
    
    public LoginResponse() {
    }
    
    public LoginResponse(String token, CurrentUserDto user) {
        this.token = token;
        this.user = user;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public CurrentUserDto getUser() {
        return user;
    }
    
    public void setUser(CurrentUserDto user) {
        this.user = user;
    }
}
