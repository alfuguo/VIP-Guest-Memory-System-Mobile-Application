package com.restaurant.vip.service;

import com.restaurant.vip.dto.NotificationResponse;
import com.restaurant.vip.dto.NotificationSummaryResponse;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Visit;
import com.restaurant.vip.repository.GuestRepository;
import com.restaurant.vip.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    private final GuestRepository guestRepository;
    private final VisitRepository visitRepository;
    
    @Autowired
    public NotificationService(GuestRepository guestRepository, VisitRepository visitRepository) {
        this.guestRepository = guestRepository;
        this.visitRepository = visitRepository;
    }
    
    /**
     * Get all notifications for today's service
     * Requirements: 5.1, 5.2, 5.3, 5.4, 5.6, 5.7
     */
    public NotificationSummaryResponse getAllNotifications() {
        List<NotificationResponse> notifications = new ArrayList<>();
        
        // Add pre-arrival notifications (guests with visits today)
        notifications.addAll(getPreArrivalNotifications());
        
        // Add special occasion notifications
        notifications.addAll(getSpecialOccasionNotifications());
        
        // Add returning guest notifications
        notifications.addAll(getReturningGuestNotifications());
        
        // Sort by priority and then by notification type
        notifications.sort(Comparator
            .comparing((NotificationResponse n) -> n.getPriority() != null ? n.getPriority().ordinal() : 0)
            .reversed()
            .thenComparing(n -> n.getNotificationType().ordinal()));
        
        return new NotificationSummaryResponse(notifications);
    }
    
    /**
     * Get pre-arrival notifications for guests visiting today
     * Requirements: 5.1, 5.2, 5.6
     */
    public List<NotificationResponse> getPreArrivalNotifications() {
        LocalDate today = LocalDate.now();
        List<Visit> todaysVisits = visitRepository.findByVisitDateOrderByVisitTimeAsc(today);
        
        return todaysVisits.stream()
            .map(visit -> createPreArrivalNotification(visit.getGuest(), visit))
            .collect(Collectors.toList());
    }
    
    /**
     * Get special occasion notifications (birthdays and anniversaries)
     * Requirements: 5.3, 5.4
     */
    public List<NotificationResponse> getSpecialOccasionNotifications() {
        List<NotificationResponse> notifications = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Get guests with birthdays today or within next 7 days
        List<Guest> birthdayGuests = guestRepository.findGuestsWithUpcomingBirthdays(
            today.getMonthValue(), today.getDayOfMonth(),
            today.plusDays(7).getMonthValue(), today.plusDays(7).getDayOfMonth()
        );
        
        for (Guest guest : birthdayGuests) {
            if (guest.getBirthday() != null) {
                LocalDate birthdayThisYear = guest.getBirthday().withYear(today.getYear());
                if (birthdayThisYear.isBefore(today)) {
                    birthdayThisYear = birthdayThisYear.plusYears(1);
                }
                
                NotificationResponse notification = createSpecialOccasionNotification(
                    guest, NotificationResponse.NotificationType.BIRTHDAY, birthdayThisYear
                );
                notifications.add(notification);
            }
        }
        
        // Get guests with anniversaries today or within next 7 days
        List<Guest> anniversaryGuests = guestRepository.findGuestsWithUpcomingAnniversaries(
            today.getMonthValue(), today.getDayOfMonth(),
            today.plusDays(7).getMonthValue(), today.plusDays(7).getDayOfMonth()
        );
        
        for (Guest guest : anniversaryGuests) {
            if (guest.getAnniversary() != null) {
                LocalDate anniversaryThisYear = guest.getAnniversary().withYear(today.getYear());
                if (anniversaryThisYear.isBefore(today)) {
                    anniversaryThisYear = anniversaryThisYear.plusYears(1);
                }
                
                NotificationResponse notification = createSpecialOccasionNotification(
                    guest, NotificationResponse.NotificationType.ANNIVERSARY, anniversaryThisYear
                );
                notifications.add(notification);
            }
        }
        
        return notifications;
    }
    
    /**
     * Get returning guest notifications (guests who haven't visited in over 6 months)
     * Requirements: 5.7
     */
    public List<NotificationResponse> getReturningGuestNotifications() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        List<Guest> returningGuests = guestRepository.findReturningGuests(sixMonthsAgo);
        
        return returningGuests.stream()
            .map(this::createReturningGuestNotification)
            .collect(Collectors.toList());
    }
    
    /**
     * Get notifications by type
     */
    public List<NotificationResponse> getNotificationsByType(NotificationResponse.NotificationType type) {
        switch (type) {
            case PRE_ARRIVAL:
                return getPreArrivalNotifications();
            case BIRTHDAY:
            case ANNIVERSARY:
            case SPECIAL_OCCASION:
                return getSpecialOccasionNotifications().stream()
                    .filter(n -> n.getNotificationType() == type)
                    .collect(Collectors.toList());
            case RETURNING_GUEST:
                return getReturningGuestNotifications();
            default:
                return new ArrayList<>();
        }
    }
    
    private NotificationResponse createPreArrivalNotification(Guest guest, Visit visit) {
        NotificationResponse notification = new NotificationResponse(
            guest.getId(),
            guest.getFirstName(),
            guest.getLastName(),
            guest.getPhone(),
            NotificationResponse.NotificationType.PRE_ARRIVAL,
            String.format("%s is arriving today at %s", guest.getFullName(), visit.getVisitTime())
        );
        
        populateGuestDetails(notification, guest);
        
        // Set priority based on dietary restrictions or special preferences
        if (guest.getDietaryRestrictions() != null && !guest.getDietaryRestrictions().isEmpty()) {
            notification.setPriority(NotificationResponse.NotificationPriority.HIGH);
        } else if (guest.getSeatingPreference() != null && !guest.getSeatingPreference().trim().isEmpty()) {
            notification.setPriority(NotificationResponse.NotificationPriority.MEDIUM);
        } else {
            notification.setPriority(NotificationResponse.NotificationPriority.LOW);
        }
        
        return notification;
    }
    
    private NotificationResponse createSpecialOccasionNotification(Guest guest, 
                                                                NotificationResponse.NotificationType type, 
                                                                LocalDate occasionDate) {
        String occasionName = type == NotificationResponse.NotificationType.BIRTHDAY ? "birthday" : "anniversary";
        LocalDate today = LocalDate.now();
        
        String message;
        NotificationResponse.NotificationPriority priority;
        
        if (occasionDate.equals(today)) {
            message = String.format("Today is %s's %s! 🎉", guest.getFullName(), occasionName);
            priority = NotificationResponse.NotificationPriority.URGENT;
        } else {
            long daysUntil = ChronoUnit.DAYS.between(today, occasionDate);
            message = String.format("%s's %s is in %d day%s", 
                guest.getFullName(), occasionName, daysUntil, daysUntil == 1 ? "" : "s");
            priority = daysUntil <= 3 ? NotificationResponse.NotificationPriority.HIGH : 
                      NotificationResponse.NotificationPriority.MEDIUM;
        }
        
        NotificationResponse notification = new NotificationResponse(
            guest.getId(),
            guest.getFirstName(),
            guest.getLastName(),
            guest.getPhone(),
            type,
            message
        );
        
        notification.setSpecialOccasionDate(occasionDate);
        notification.setPriority(priority);
        populateGuestDetails(notification, guest);
        
        return notification;
    }
    
    private NotificationResponse createReturningGuestNotification(Guest guest) {
        Visit lastVisit = guest.getLastVisit();
        long daysSinceLastVisit = lastVisit != null ? 
            ChronoUnit.DAYS.between(lastVisit.getVisitDate(), LocalDate.now()) : 0;
        
        String message = String.format("%s is a returning guest (last visit: %s)", 
            guest.getFullName(), 
            lastVisit != null ? lastVisit.getVisitDate().toString() : "Unknown");
        
        NotificationResponse notification = new NotificationResponse(
            guest.getId(),
            guest.getFirstName(),
            guest.getLastName(),
            guest.getPhone(),
            NotificationResponse.NotificationType.RETURNING_GUEST,
            message
        );
        
        notification.setDaysSinceLastVisit(daysSinceLastVisit);
        notification.setPriority(NotificationResponse.NotificationPriority.MEDIUM);
        populateGuestDetails(notification, guest);
        
        return notification;
    }
    
    private void populateGuestDetails(NotificationResponse notification, Guest guest) {
        notification.setPhotoUrl(guest.getPhotoUrl());
        notification.setSeatingPreference(guest.getSeatingPreference());
        notification.setDietaryRestrictions(guest.getDietaryRestrictions());
        notification.setFavoriteDrinks(guest.getFavoriteDrinks());
        notification.setVisitCount(guest.getVisitCount());
        
        Visit lastVisit = guest.getLastVisit();
        if (lastVisit != null) {
            notification.setLastVisitDate(lastVisit.getVisitDate());
            notification.setLastVisitNotes(lastVisit.getServiceNotes());
        }
    }
}