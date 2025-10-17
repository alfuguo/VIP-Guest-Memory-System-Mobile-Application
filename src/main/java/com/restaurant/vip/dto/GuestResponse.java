package com.restaurant.vip.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GuestResponse {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String photoUrl;
    private String seatingPreference;
    private List<String> dietaryRestrictions;
    private List<String> favoriteDrinks;
    private LocalDate birthday;
    private LocalDate anniversary;
    private String notes;
    private LocalDateTime lastVisit;
    private Integer visitCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    
    // Constructors
    public GuestResponse() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    
    public String getSeatingPreference() {
        return seatingPreference;
    }
    
    public void setSeatingPreference(String seatingPreference) {
        this.seatingPreference = seatingPreference;
    }
    
    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }
    
    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }
    
    public List<String> getFavoriteDrinks() {
        return favoriteDrinks;
    }
    
    public void setFavoriteDrinks(List<String> favoriteDrinks) {
        this.favoriteDrinks = favoriteDrinks;
    }
    
    public LocalDate getBirthday() {
        return birthday;
    }
    
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
    
    public LocalDate getAnniversary() {
        return anniversary;
    }
    
    public void setAnniversary(LocalDate anniversary) {
        this.anniversary = anniversary;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getLastVisit() {
        return lastVisit;
    }
    
    public void setLastVisit(LocalDateTime lastVisit) {
        this.lastVisit = lastVisit;
    }
    
    public Integer getVisitCount() {
        return visitCount;
    }
    
    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
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
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    // Utility methods
    public String getFullName() {
        if (lastName != null && !lastName.trim().isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }
}