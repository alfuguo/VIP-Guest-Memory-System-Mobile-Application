package com.restaurant.vip.dto;

import jakarta.validation.constraints.Size;

public class VisitNotesRequest {
    
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
    
    // Constructors
    public VisitNotesRequest() {}
    
    public VisitNotesRequest(String notes) {
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}