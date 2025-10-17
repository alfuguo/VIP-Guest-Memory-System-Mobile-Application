package com.restaurant.vip.service;

import com.restaurant.vip.dto.NotificationResponse;
import com.restaurant.vip.dto.NotificationSummaryResponse;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Visit;
import com.restaurant.vip.repository.GuestRepository;
import com.restaurant.vip.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Guest testGuest;
    private Visit testVisit;
    private Guest birthdayGuest;
    private Guest anniversaryGuest;
    private Guest returningGuest;

    @BeforeEach
    void setUp() {
        // Setup test guest with visit today
        testGuest = new Guest();
        testGuest.setId(1L);
        testGuest.setFirstName("John");
        testGuest.setLastName("Doe");
        testGuest.setPhone("+1234567890");
        testGuest.setSeatingPreference("Window table");
        testGuest.setDietaryRestrictions(Arrays.asList("Vegetarian"));
        testGuest.setFavoriteDrinks(Arrays.asList("Red wine"));

        // Setup test visit for today
        testVisit = new Visit();
        testVisit.setId(1L);
        testVisit.setGuest(testGuest);
        testVisit.setVisitDate(LocalDate.now());
        testVisit.setVisitTime(LocalTime.of(19, 30));
        testVisit.setServiceNotes("Regular customer");

        // Setup birthday guest
        birthdayGuest = new Guest();
        birthdayGuest.setId(2L);
        birthdayGuest.setFirstName("Jane");
        birthdayGuest.setLastName("Smith");
        birthdayGuest.setPhone("+1987654321");
        birthdayGuest.setBirthday(LocalDate.now()); // Birthday today

        // Setup anniversary guest
        anniversaryGuest = new Guest();
        anniversaryGuest.setId(3L);
        anniversaryGuest.setFirstName("Bob");
        anniversaryGuest.setLastName("Johnson");
        anniversaryGuest.setPhone("+1122334455");
        anniversaryGuest.setAnniversary(LocalDate.now().plusDays(2)); // Anniversary in 2 days

        // Setup returning guest
        returningGuest = new Guest();
        returningGuest.setId(4L);
        returningGuest.setFirstName("Alice");
        returningGuest.setLastName("Brown");
        returningGuest.setPhone("+1555666777");
        
        Visit lastVisit = new Visit();
        lastVisit.setVisitDate(LocalDate.now().minusMonths(8));
        returningGuest.setLastVisit(lastVisit);
    }

    @Test
    void getAllNotifications_Success() {
        // Arrange
        when(visitRepository.findByVisitDateOrderByVisitTimeAsc(LocalDate.now()))
                .thenReturn(Arrays.asList(testVisit));
        when(guestRepository.findGuestsWithUpcomingBirthdays(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(birthdayGuest));
        when(guestRepository.findGuestsWithUpcomingAnniversaries(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(anniversaryGuest));
        when(guestRepository.findReturningGuests(any(LocalDate.class)))
                .thenReturn(Arrays.asList(returningGuest));

        // Act
        NotificationSummaryResponse result = notificationService.getAllNotifications();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getNotifications());
        assertTrue(result.getNotifications().size() >= 3); // At least pre-arrival, birthday, anniversary, returning

        verify(visitRepository).findByVisitDateOrderByVisitTimeAsc(LocalDate.now());
        verify(guestRepository).findGuestsWithUpcomingBirthdays(anyInt(), anyInt(), anyInt(), anyInt());
        verify(guestRepository).findGuestsWithUpcomingAnniversaries(anyInt(), anyInt(), anyInt(), anyInt());
        verify(guestRepository).findReturningGuests(any(LocalDate.class));
    }

    @Test
    void getPreArrivalNotifications_Success() {
        // Arrange
        when(visitRepository.findByVisitDateOrderByVisitTimeAsc(LocalDate.now()))
                .thenReturn(Arrays.asList(testVisit));

        // Act
        List<NotificationResponse> result = notificationService.getPreArrivalNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        NotificationResponse notification = result.get(0);
        assertEquals(testGuest.getId(), notification.getGuestId());
        assertEquals(testGuest.getFirstName(), notification.getFirstName());
        assertEquals(testGuest.getLastName(), notification.getLastName());
        assertEquals(NotificationResponse.NotificationType.PRE_ARRIVAL, notification.getNotificationType());
        assertTrue(notification.getMessage().contains("arriving today"));
        assertEquals(NotificationResponse.NotificationPriority.HIGH, notification.getPriority()); // Has dietary restrictions

        verify(visitRepository).findByVisitDateOrderByVisitTimeAsc(LocalDate.now());
    }

    @Test
    void getPreArrivalNotifications_EmptyList() {
        // Arrange
        when(visitRepository.findByVisitDateOrderByVisitTimeAsc(LocalDate.now()))
                .thenReturn(Arrays.asList());

        // Act
        List<NotificationResponse> result = notificationService.getPreArrivalNotifications();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(visitRepository).findByVisitDateOrderByVisitTimeAsc(LocalDate.now());
    }

    @Test
    void getSpecialOccasionNotifications_Birthday_Success() {
        // Arrange
        when(guestRepository.findGuestsWithUpcomingBirthdays(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(birthdayGuest));
        when(guestRepository.findGuestsWithUpcomingAnniversaries(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());

        // Act
        List<NotificationResponse> result = notificationService.getSpecialOccasionNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        NotificationResponse notification = result.get(0);
        assertEquals(birthdayGuest.getId(), notification.getGuestId());
        assertEquals(NotificationResponse.NotificationType.BIRTHDAY, notification.getNotificationType());
        assertTrue(notification.getMessage().contains("birthday"));
        assertTrue(notification.getMessage().contains("Today is"));
        assertEquals(NotificationResponse.NotificationPriority.URGENT, notification.getPriority()); // Today's birthday

        verify(guestRepository).findGuestsWithUpcomingBirthdays(anyInt(), anyInt(), anyInt(), anyInt());
        verify(guestRepository).findGuestsWithUpcomingAnniversaries(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void getSpecialOccasionNotifications_Anniversary_Success() {
        // Arrange
        when(guestRepository.findGuestsWithUpcomingBirthdays(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());
        when(guestRepository.findGuestsWithUpcomingAnniversaries(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(anniversaryGuest));

        // Act
        List<NotificationResponse> result = notificationService.getSpecialOccasionNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        NotificationResponse notification = result.get(0);
        assertEquals(anniversaryGuest.getId(), notification.getGuestId());
        assertEquals(NotificationResponse.NotificationType.ANNIVERSARY, notification.getNotificationType());
        assertTrue(notification.getMessage().contains("anniversary"));
        assertTrue(notification.getMessage().contains("in 2 day"));
        assertEquals(NotificationResponse.NotificationPriority.HIGH, notification.getPriority()); // Within 3 days

        verify(guestRepository).findGuestsWithUpcomingBirthdays(anyInt(), anyInt(), anyInt(), anyInt());
        verify(guestRepository).findGuestsWithUpcomingAnniversaries(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void getReturningGuestNotifications_Success() {
        // Arrange
        when(guestRepository.findReturningGuests(any(LocalDate.class)))
                .thenReturn(Arrays.asList(returningGuest));

        // Act
        List<NotificationResponse> result = notificationService.getReturningGuestNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        NotificationResponse notification = result.get(0);
        assertEquals(returningGuest.getId(), notification.getGuestId());
        assertEquals(NotificationResponse.NotificationType.RETURNING_GUEST, notification.getNotificationType());
        assertTrue(notification.getMessage().contains("returning guest"));
        assertEquals(NotificationResponse.NotificationPriority.MEDIUM, notification.getPriority());

        verify(guestRepository).findReturningGuests(any(LocalDate.class));
    }

    @Test
    void getNotificationsByType_PreArrival_Success() {
        // Arrange
        when(visitRepository.findByVisitDateOrderByVisitTimeAsc(LocalDate.now()))
                .thenReturn(Arrays.asList(testVisit));

        // Act
        List<NotificationResponse> result = notificationService.getNotificationsByType(
                NotificationResponse.NotificationType.PRE_ARRIVAL);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationResponse.NotificationType.PRE_ARRIVAL, result.get(0).getNotificationType());

        verify(visitRepository).findByVisitDateOrderByVisitTimeAsc(LocalDate.now());
    }

    @Test
    void getNotificationsByType_Birthday_Success() {
        // Arrange
        when(guestRepository.findGuestsWithUpcomingBirthdays(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(birthdayGuest));
        when(guestRepository.findGuestsWithUpcomingAnniversaries(anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());

        // Act
        List<NotificationResponse> result = notificationService.getNotificationsByType(
                NotificationResponse.NotificationType.BIRTHDAY);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationResponse.NotificationType.BIRTHDAY, result.get(0).getNotificationType());

        verify(guestRepository).findGuestsWithUpcomingBirthdays(anyInt(), anyInt(), anyInt(), anyInt());
        verify(guestRepository).findGuestsWithUpcomingAnniversaries(anyInt(), anyInt(), anyInt(), anyInt());
    }

    @Test
    void getNotificationsByType_ReturningGuest_Success() {
        // Arrange
        when(guestRepository.findReturningGuests(any(LocalDate.class)))
                .thenReturn(Arrays.asList(returningGuest));

        // Act
        List<NotificationResponse> result = notificationService.getNotificationsByType(
                NotificationResponse.NotificationType.RETURNING_GUEST);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationResponse.NotificationType.RETURNING_GUEST, result.get(0).getNotificationType());

        verify(guestRepository).findReturningGuests(any(LocalDate.class));
    }

    @Test
    void getNotificationsByType_UnknownType_ReturnsEmpty() {
        // Act
        List<NotificationResponse> result = notificationService.getNotificationsByType(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void preArrivalNotification_PriorityBasedOnDietaryRestrictions() {
        // Arrange
        Guest guestWithDietaryRestrictions = new Guest();
        guestWithDietaryRestrictions.setId(1L);
        guestWithDietaryRestrictions.setFirstName("John");
        guestWithDietaryRestrictions.setLastName("Doe");
        guestWithDietaryRestrictions.setDietaryRestrictions(Arrays.asList("Gluten-free", "Nut allergy"));

        Visit visit = new Visit();
        visit.setGuest(guestWithDietaryRestrictions);
        visit.setVisitDate(LocalDate.now());
        visit.setVisitTime(LocalTime.of(19, 30));

        when(visitRepository.findByVisitDateOrderByVisitTimeAsc(LocalDate.now()))
                .thenReturn(Arrays.asList(visit));

        // Act
        List<NotificationResponse> result = notificationService.getPreArrivalNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationResponse.NotificationPriority.HIGH, result.get(0).getPriority());
    }

    @Test
    void preArrivalNotification_PriorityBasedOnSeatingPreference() {
        // Arrange
        Guest guestWithSeatingPreference = new Guest();
        guestWithSeatingPreference.setId(1L);
        guestWithSeatingPreference.setFirstName("Jane");
        guestWithSeatingPreference.setLastName("Smith");
        guestWithSeatingPreference.setSeatingPreference("Quiet corner");

        Visit visit = new Visit();
        visit.setGuest(guestWithSeatingPreference);
        visit.setVisitDate(LocalDate.now());
        visit.setVisitTime(LocalTime.of(19, 30));

        when(visitRepository.findByVisitDateOrderByVisitTimeAsc(LocalDate.now()))
                .thenReturn(Arrays.asList(visit));

        // Act
        List<NotificationResponse> result = notificationService.getPreArrivalNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationResponse.NotificationPriority.MEDIUM, result.get(0).getPriority());
    }

    @Test
    void preArrivalNotification_LowPriorityDefault() {
        // Arrange
        Guest basicGuest = new Guest();
        basicGuest.setId(1L);
        basicGuest.setFirstName("Bob");
        basicGuest.setLastName("Johnson");

        Visit visit = new Visit();
        visit.setGuest(basicGuest);
        visit.setVisitDate(LocalDate.now());
        visit.setVisitTime(LocalTime.of(19, 30));

        when(visitRepository.findByVisitDateOrderByVisitTimeAsc(LocalDate.now()))
                .thenReturn(Arrays.asList(visit));

        // Act
        List<NotificationResponse> result = notificationService.getPreArrivalNotifications();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationResponse.NotificationPriority.LOW, result.get(0).getPriority());
    }
}