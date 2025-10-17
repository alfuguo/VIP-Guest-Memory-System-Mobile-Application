package com.restaurant.vip.dto;

import com.restaurant.vip.entity.Visit;

import java.time.LocalDateTime;

public class VisitNotesResponse {
    
    private Long visitId;
    private String notes;
    private String originalStaffName;
    private Long originalStaffId;
    private String lastModifiedByStaffName;
    private Long lastModifiedByStaffId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canEdit;
    
    // Constructors
    public VisitNotesResponse() {}
    
    public VisitNotesResponse(Visit visit, boolean canEdit) {
        this.visitId = visit.getId();
        this.notes = visit.getServiceNotes();
        this.originalStaffName = visit.getStaff().getFirstName() + " " + visit.getStaff().getLastName();
        this.originalStaffId = visit.getStaff().getId();
        this.lastModifiedByStaffName = this.originalStaffName; // For now, same as original
        this.lastModifiedByStaffId = this.originalStaffId;
        this.createdAt = visit.getCreatedAt();
        this.updatedAt = visit.getUpdatedAt();
        this.canEdit = canEdit;
    }
    
    // Getters and Setters
    public Long getVisitId() {
        return visitId;
    }
    
    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getOriginalStaffName() {
        return originalStaffName;
    }
    
    public void setOriginalStaffName(String originalStaffName) {
        this.originalStaffName = originalStaffName;
    }
    
    public Long getOriginalStaffId() {
        return originalStaffId;
    }
    
    public void setOriginalStaffId(Long originalStaffId) {
        this.originalStaffId = originalStaffId;
    }
    
    public String getLastModifiedByStaffName() {
        return lastModifiedByStaffName;
    }
    
    public void setLastModifiedByStaffName(String lastModifiedByStaffName) {
        this.lastModifiedByStaffName = lastModifiedByStaffName;
    }
    
    public Long getLastModifiedByStaffId() {
        return lastModifiedByStaffId;
    }
    
    public void setLastModifiedByStaffId(Long lastModifiedByStaffId) {
        this.lastModifiedByStaffId = lastModifiedByStaffId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isCanEdit() {
        return canEdit;
    }
    
    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
    
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
}