package com.restaurant.vip.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationResponse {
    
    @JsonProperty("id")
    private Long guestId;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("photoUrl")
    private String photoUrl;
    
    @JsonProperty("notificationType")
    private NotificationType notificationType;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("priority")
    private NotificationPriority priority;
    
    @JsonProperty("seatingPreference")
    private String seatingPreference;
    
    @JsonProperty("dietaryRestrictions")
    private List<String> dietaryRestrictions;
    
    @JsonProperty("favoriteDrinks")
    private List<String> favoriteDrinks;
    
    @JsonProperty("lastVisitDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastVisitDate;
    
    @JsonProperty("lastVisitNotes")
    private String lastVisitNotes;
    
    @JsonProperty("visitCount")
    private Integer visitCount;
    
    @JsonProperty("specialOccasionDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate specialOccasionDate;
    
    @JsonProperty("daysSinceLastVisit")
    private Long daysSinceLastVisit;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    // Constructors
    public NotificationResponse() {}
    
    public NotificationResponse(Long guestId, String firstName, String lastName, 
                              String phone, NotificationType notificationType, String message) {
        this.guestId = guestId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.notificationType = notificationType;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getGuestId() {
        return guestId;
    }
    
    public void setGuestId(Long guestId) {
        this.guestId = guestId;
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
    
    public String getPhotoUrl() {
        return photoUrl;
    }
    
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    
    public NotificationType getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public NotificationPriority getPriority() {
        return priority;
    }
    
    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
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
    
    public LocalDate getLastVisitDate() {
        return lastVisitDate;
    }
    
    public void setLastVisitDate(LocalDate lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }
    
    public String getLastVisitNotes() {
        return lastVisitNotes;
    }
    
    public void setLastVisitNotes(String lastVisitNotes) {
        this.lastVisitNotes = lastVisitNotes;
    }
    
    public Integer getVisitCount() {
        return visitCount;
    }
    
    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }
    
    public LocalDate getSpecialOccasionDate() {
        return specialOccasionDate;
    }
    
    public void setSpecialOccasionDate(LocalDate specialOccasionDate) {
        this.specialOccasionDate = specialOccasionDate;
    }
    
    public Long getDaysSinceLastVisit() {
        return daysSinceLastVisit;
    }
    
    public void setDaysSinceLastVisit(Long daysSinceLastVisit) {
        this.daysSinceLastVisit = daysSinceLastVisit;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public String getFullName() {
        if (lastName != null && !lastName.trim().isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }
    
    public enum NotificationType {
        PRE_ARRIVAL,
        BIRTHDAY,
        ANNIVERSARY,
        RETURNING_GUEST,
        DIETARY_RESTRICTION,
        SPECIAL_OCCASION
    }
    
    public enum NotificationPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}