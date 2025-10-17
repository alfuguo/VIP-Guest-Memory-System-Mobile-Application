package com.restaurant.vip.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class VisitUpdateRequest {
    
    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;
    
    @NotNull(message = "Visit time is required")
    private LocalTime visitTime;
    
    @Min(value = 1, message = "Party size must be at least 1")
    private Integer partySize = 1;
    
    @Size(max = 10, message = "Table number must not exceed 10 characters")
    private String tableNumber;
    
    private String serviceNotes;
    
    // Constructors
    public VisitUpdateRequest() {}
    
    public VisitUpdateRequest(LocalDate visitDate, LocalTime visitTime) {
        this.visitDate = visitDate;
        this.visitTime = visitTime;
    }
    
    // Getters and Setters
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
}