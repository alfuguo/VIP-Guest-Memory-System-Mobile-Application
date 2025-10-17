package com.restaurant.vip.dto;

import com.restaurant.vip.entity.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class VisitHistoryResponse {
    
    private Long guestId;
    private String guestName;
    private long totalVisits;
    private LocalDate firstVisitDate;
    private LocalDate lastVisitDate;
    private List<VisitSummary> visits;
    
    // Constructors
    public VisitHistoryResponse() {}
    
    public VisitHistoryResponse(Long guestId, String guestName, long totalVisits, 
                               LocalDate firstVisitDate, LocalDate lastVisitDate, 
                               List<VisitSummary> visits) {
        this.guestId = guestId;
        this.guestName = guestName;
        this.totalVisits = totalVisits;
        this.firstVisitDate = firstVisitDate;
        this.lastVisitDate = lastVisitDate;
        this.visits = visits;
    }
    
    // Getters and Setters
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
    
    public long getTotalVisits() {
        return totalVisits;
    }
    
    public void setTotalVisits(long totalVisits) {
        this.totalVisits = totalVisits;
    }
    
    public LocalDate getFirstVisitDate() {
        return firstVisitDate;
    }
    
    public void setFirstVisitDate(LocalDate firstVisitDate) {
        this.firstVisitDate = firstVisitDate;
    }
    
    public LocalDate getLastVisitDate() {
        return lastVisitDate;
    }
    
    public void setLastVisitDate(LocalDate lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }
    
    public List<VisitSummary> getVisits() {
        return visits;
    }
    
    public void setVisits(List<VisitSummary> visits) {
        this.visits = visits;
    }
    
    // Inner class for visit summary in timeline
    public static class VisitSummary {
        private Long id;
        private LocalDate visitDate;
        private LocalTime visitTime;
        private Integer partySize;
        private String tableNumber;
        private String staffName;
        private String serviceNotes;
        private boolean hasNotes;
        private LocalDateTime createdAt;
        
        public VisitSummary() {}
        
        public VisitSummary(Visit visit) {
            this.id = visit.getId();
            this.visitDate = visit.getVisitDate();
            this.visitTime = visit.getVisitTime();
            this.partySize = visit.getPartySize();
            this.tableNumber = visit.getTableNumber();
            this.staffName = visit.getStaff().getFirstName() + " " + visit.getStaff().getLastName();
            this.serviceNotes = visit.getServiceNotes();
            this.hasNotes = visit.getServiceNotes() != null && !visit.getServiceNotes().trim().isEmpty();
            this.createdAt = visit.getCreatedAt();
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
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
        
        public String getStaffName() {
            return staffName;
        }
        
        public void setStaffName(String staffName) {
            this.staffName = staffName;
        }
        
        public String getServiceNotes() {
            return serviceNotes;
        }
        
        public void setServiceNotes(String serviceNotes) {
            this.serviceNotes = serviceNotes;
        }
        
        public boolean isHasNotes() {
            return hasNotes;
        }
        
        public void setHasNotes(boolean hasNotes) {
            this.hasNotes = hasNotes;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
        
        public LocalDateTime getVisitDateTime() {
            return LocalDateTime.of(visitDate, visitTime);
        }
    }
}