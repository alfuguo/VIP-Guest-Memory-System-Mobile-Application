package com.restaurant.vip.dto;

public class DuplicateCheckResponse {
    
    private boolean exists;
    private String phone;
    private String message;
    private GuestResponse existingGuest;
    
    // Constructors
    public DuplicateCheckResponse() {}
    
    public DuplicateCheckResponse(boolean exists, String phone) {
        this.exists = exists;
        this.phone = phone;
    }
    
    // Getters and Setters
    public boolean isExists() {
        return exists;
    }
    
    public void setExists(boolean exists) {
        this.exists = exists;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public GuestResponse getExistingGuest() {
        return existingGuest;
    }
    
    public void setExistingGuest(GuestResponse existingGuest) {
        this.existingGuest = existingGuest;
    }
}