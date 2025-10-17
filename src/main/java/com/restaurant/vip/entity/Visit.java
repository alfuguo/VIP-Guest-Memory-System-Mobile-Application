package com.restaurant.vip.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "visits")
public class Visit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "visit_date", nullable = false)
    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;
    
    @Column(name = "visit_time", nullable = false)
    @NotNull(message = "Visit time is required")
    private LocalTime visitTime;
    
    @Column(name = "party_size")
    @Min(value = 1, message = "Party size must be at least 1")
    private Integer partySize = 1;
    
    @Column(name = "table_number", length = 10)
    @Size(max = 10, message = "Table number must not exceed 10 characters")
    private String tableNumber;
    
    @Column(name = "service_notes", columnDefinition = "TEXT")
    private String serviceNotes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    @NotNull(message = "Guest is required")
    private Guest guest;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false)
    @NotNull(message = "Staff member is required")
    private Staff staff;
    
    // Constructors
    public Visit() {}
    
    public Visit(Guest guest, Staff staff, LocalDate visitDate, LocalTime visitTime) {
        this.guest = guest;
        this.staff = staff;
        this.visitDate = visitDate;
        this.visitTime = visitTime;
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
    
    public Guest getGuest() {
        return guest;
    }
    
    public void setGuest(Guest guest) {
        this.guest = guest;
    }
    
    public Staff getStaff() {
        return staff;
    }
    
    public void setStaff(Staff staff) {
        this.staff = staff;
    }
    
    // Utility methods
    public LocalDateTime getVisitDateTime() {
        return LocalDateTime.of(visitDate, visitTime);
    }
    
    public boolean isToday() {
        return visitDate.equals(LocalDate.now());
    }
    
    public boolean isRecent() {
        return visitDate.isAfter(LocalDate.now().minusDays(7));
    }
    
    public String getFormattedDateTime() {
        return visitDate.toString() + " " + visitTime.toString();
    }
}