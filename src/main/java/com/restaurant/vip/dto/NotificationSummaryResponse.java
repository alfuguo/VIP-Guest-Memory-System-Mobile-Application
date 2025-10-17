package com.restaurant.vip.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NotificationSummaryResponse {
    
    @JsonProperty("totalNotifications")
    private int totalNotifications;
    
    @JsonProperty("preArrivalCount")
    private int preArrivalCount;
    
    @JsonProperty("specialOccasionCount")
    private int specialOccasionCount;
    
    @JsonProperty("returningGuestCount")
    private int returningGuestCount;
    
    @JsonProperty("highPriorityCount")
    private int highPriorityCount;
    
    @JsonProperty("notifications")
    private List<NotificationResponse> notifications;
    
    // Constructors
    public NotificationSummaryResponse() {}
    
    public NotificationSummaryResponse(List<NotificationResponse> notifications) {
        this.notifications = notifications;
        this.totalNotifications = notifications.size();
        calculateCounts();
    }
    
    private void calculateCounts() {
        this.preArrivalCount = (int) notifications.stream()
            .filter(n -> n.getNotificationType() == NotificationResponse.NotificationType.PRE_ARRIVAL)
            .count();
            
        this.specialOccasionCount = (int) notifications.stream()
            .filter(n -> n.getNotificationType() == NotificationResponse.NotificationType.BIRTHDAY ||
                        n.getNotificationType() == NotificationResponse.NotificationType.ANNIVERSARY ||
                        n.getNotificationType() == NotificationResponse.NotificationType.SPECIAL_OCCASION)
            .count();
            
        this.returningGuestCount = (int) notifications.stream()
            .filter(n -> n.getNotificationType() == NotificationResponse.NotificationType.RETURNING_GUEST)
            .count();
            
        this.highPriorityCount = (int) notifications.stream()
            .filter(n -> n.getPriority() == NotificationResponse.NotificationPriority.HIGH ||
                        n.getPriority() == NotificationResponse.NotificationPriority.URGENT)
            .count();
    }
    
    // Getters and Setters
    public int getTotalNotifications() {
        return totalNotifications;
    }
    
    public void setTotalNotifications(int totalNotifications) {
        this.totalNotifications = totalNotifications;
    }
    
    public int getPreArrivalCount() {
        return preArrivalCount;
    }
    
    public void setPreArrivalCount(int preArrivalCount) {
        this.preArrivalCount = preArrivalCount;
    }
    
    public int getSpecialOccasionCount() {
        return specialOccasionCount;
    }
    
    public void setSpecialOccasionCount(int specialOccasionCount) {
        this.specialOccasionCount = specialOccasionCount;
    }
    
    public int getReturningGuestCount() {
        return returningGuestCount;
    }
    
    public void setReturningGuestCount(int returningGuestCount) {
        this.returningGuestCount = returningGuestCount;
    }
    
    public int getHighPriorityCount() {
        return highPriorityCount;
    }
    
    public void setHighPriorityCount(int highPriorityCount) {
        this.highPriorityCount = highPriorityCount;
    }
    
    public List<NotificationResponse> getNotifications() {
        return notifications;
    }
    
    public void setNotifications(List<NotificationResponse> notifications) {
        this.notifications = notifications;
        if (notifications != null) {
            this.totalNotifications = notifications.size();
            calculateCounts();
        }
    }
}