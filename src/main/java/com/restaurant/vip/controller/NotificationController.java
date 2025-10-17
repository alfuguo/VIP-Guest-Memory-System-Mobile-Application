package com.restaurant.vip.controller;

import com.restaurant.vip.dto.NotificationResponse;
import com.restaurant.vip.dto.NotificationSummaryResponse;
import com.restaurant.vip.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Get all notifications summary
     * Requirements: 5.1, 5.2, 5.3, 5.4, 5.6, 5.7
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<NotificationSummaryResponse> getAllNotifications() {
        NotificationSummaryResponse notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get pre-arrival notifications
     * Requirements: 5.1, 5.2, 5.6
     */
    @GetMapping("/pre-arrival")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<NotificationResponse>> getPreArrivalNotifications() {
        List<NotificationResponse> notifications = notificationService.getPreArrivalNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get special occasion notifications
     * Requirements: 5.3, 5.4
     */
    @GetMapping("/special-occasions")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<NotificationResponse>> getSpecialOccasionNotifications() {
        List<NotificationResponse> notifications = notificationService.getSpecialOccasionNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get returning guest notifications
     * Requirements: 5.7
     */
    @GetMapping("/returning-guests")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<NotificationResponse>> getReturningGuestNotifications() {
        List<NotificationResponse> notifications = notificationService.getReturningGuestNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByType(
            @PathVariable String type) {
        
        try {
            NotificationResponse.NotificationType notificationType = 
                NotificationResponse.NotificationType.valueOf(type.toUpperCase());
            List<NotificationResponse> notifications = 
                notificationService.getNotificationsByType(notificationType);
            return ResponseEntity.ok(notifications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Acknowledge notification (placeholder for future implementation)
     * This endpoint is prepared for when notification acknowledgment is needed
     */
    @PostMapping("/{guestId}/acknowledge")
    @PreAuthorize("hasAnyRole('HOST', 'SERVER', 'MANAGER')")
    public ResponseEntity<Void> acknowledgeNotification(
            @PathVariable Long guestId) {
        
        // Placeholder implementation - in the future this could track which notifications
        // have been acknowledged by staff to avoid showing them repeatedly
        return ResponseEntity.ok().build();
    }
}