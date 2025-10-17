package com.restaurant.vip.dto;

import com.restaurant.vip.validation.NoSqlInjection;
import com.restaurant.vip.validation.SafeHtml;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class VisitCreateRequest {
    
    @NotNull(message = "Guest ID is required")
    private Long guestId;
    
    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;
    
    @NotNull(message = "Visit time is required")
    private LocalTime visitTime;
    
    @Min(value = 1, message = "Party size must be at least 1")
    private Integer partySize = 1;
    
    @Size(max = 10, message = "Table number must not exceed 10 characters")
    @SafeHtml(maxLength = 10)
    @NoSqlInjection
    private String tableNumber;
    
    @SafeHtml(maxLength = 1000, allowBasicFormatting = true)
    @NoSqlInjection
    private String serviceNotes;
    
    // Constructors
    public VisitCreateRequest() {}
    
    public VisitCreateRequest(Long guestId, LocalDate visitDate, LocalTime visitTime) {
        this.guestId = guestId;
        this.visitDate = visitDate;
        this.visitTime = visitTime;
    }
    
    // Getters and Setters
    public Long getGuestId() {
        return guestId;
    }
    
    public void setGuestId(Long guestId) {
        this.guestId = guestId;
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
}