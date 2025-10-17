package com.restaurant.vip.dto;

import com.restaurant.vip.entity.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class VisitResponse {
    
    private Long id;
    private Long guestId;
    private String guestName;
    private Long staffId;
    private String staffName;
    private LocalDate visitDate;
    private LocalTime visitTime;
    private Integer partySize;
    private String tableNumber;
    private String serviceNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public VisitResponse() {}
    
    public VisitResponse(Visit visit) {
        this.id = visit.getId();
        this.guestId = visit.getGuest().getId();
        this.guestName = visit.getGuest().getFirstName() + 
                        (visit.getGuest().getLastName() != null ? " " + visit.getGuest().getLastName() : "");
        this.staffId = visit.getStaff().getId();
        this.staffName = visit.getStaff().getFirstName() + " " + visit.getStaff().getLastName();
        this.visitDate = visit.getVisitDate();
        this.visitTime = visit.getVisitTime();
        this.partySize = visit.getPartySize();
        this.tableNumber = visit.getTableNumber();
        this.serviceNotes = visit.getServiceNotes();
        this.createdAt = visit.getCreatedAt();
        this.updatedAt = visit.getUpdatedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getGuestId() {
        return guestId;
    }
    
    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }
    
    public String getGuestName() {
        return guestName;
    }
    
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
    
    public Long getStaffId() {
        return staffId;
    }
    
    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }
    
    public String getStaffName() {
        return staffName;
    }
    
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
    
    public LocalDate getVisitDate() {
        return visitDate;
    }
    
    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }
    
    public LocalTime getVisitTime() {
        return visitTime;
    }
    
    public void setVisitTime(LocalTime visitTime) {
        this.visitTime = visitTime;
    }
    
    public Integer getPartySize() {
        return partySize;
    }
    
    public void setPartySize(Integer partySize) {
        this.partySize = partySize;
    }
    
    public String getTableNumber() {
        return tableNumber;
    }
    
    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }
    
    public String getServiceNotes() {
        return serviceNotes;
    }
    
    public void setServiceNotes(String serviceNotes) {
        this.serviceNotes = serviceNotes;
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
    
    // Utility methods
    public LocalDateTime getVisitDateTime() {
        return LocalDateTime.of(visitDate, visitTime);
    }
    
    public String getFormattedDateTime() {
        return visitDate.toString() + " " + visitTime.toString();
    }
}