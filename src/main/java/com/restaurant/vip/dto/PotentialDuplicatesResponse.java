package com.restaurant.vip.dto;

import java.util.List;

public class PotentialDuplicatesResponse {
    
    private boolean hasPotentialDuplicates;
    private String message;
    private List<GuestResponse> potentialDuplicates;
    private int count;
    
    // Constructors
    public PotentialDuplicatesResponse() {}
    
    public PotentialDuplicatesResponse(List<GuestResponse> potentialDuplicates) {
        this.potentialDuplicates = potentialDuplicates;
        this.count = potentialDuplicates != null ? potentialDuplicates.size() : 0;
        this.hasPotentialDuplicates = this.count > 0;
        
        if (this.hasPotentialDuplicates) {
            this.message = String.format("Found %d potential duplicate(s) with similar information", this.count);
        } else {
            this.message = "No potential duplicates found";
        }
    }
    
    // Getters and Setters
    public boolean isHasPotentialDuplicates() {
        return hasPotentialDuplicates;
    }
    
    public void setHasPotentialDuplicates(boolean hasPotentialDuplicates) {
        this.hasPotentialDuplicates = hasPotentialDuplicates;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<GuestResponse> getPotentialDuplicates() {
        return potentialDuplicates;
    }
    
    public void setPotentialDuplicates(List<GuestResponse> potentialDuplicates) {
        this.potentialDuplicates = potentialDuplicates;
        this.count = potentialDuplicates != null ? potentialDuplicates.size() : 0;
        this.hasPotentialDuplicates = this.count > 0;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
}