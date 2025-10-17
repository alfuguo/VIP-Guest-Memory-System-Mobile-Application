package com.restaurant.vip.integration;

import com.restaurant.vip.dto.*;
import com.restaurant.vip.entity.Guest;
import com.restaurant.vip.entity.Staff;
import com.restaurant.vip.entity.Visit;
import com.restaurant.vip.enums.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Builder class for creating test data objects used in integration tests.
 * Provides convenient methods to create consistent test data.
 */
public class TestDataBuilder {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Staff builders
    public static Staff createManagerStaff() {
        Staff staff = new Staff();
        staff.setEmail("manager@restaurant.com");
        staff.setPasswordHash(passwordEncoder.encode("password123"));
        staff.setFirstName("John");
        staff.setLastName("Manager");
        staff.setRole(Role.MANAGER);
        staff.setActive(true);
        return staff;
    }

    public static Staff createHostStaff() {
        Staff staff = new Staff();
        staff.setEmail("host@restaurant.com");
        staff.setPasswordHash(passwordEncoder.encode("password123"));
        staff.setFirstName("Jane");
        staff.setLastName("Host");
        staff.setRole(Role.HOST);
        staff.setActive(true);
        return staff;
    }

    public static Staff createServerStaff() {
        Staff staff = new Staff();
        staff.setEmail("server@restaurant.com");
        staff.setPasswordHash(passwordEncoder.encode("password123"));
        staff.setFirstName("Bob");
        staff.setLastName("Server");
        staff.setRole(Role.SERVER);
        staff.setActive(true);
        return staff;
    }

    // Guest builders
    public static Guest createBasicGuest() {
        Guest guest = new Guest();
        guest.setFirstName("John");
        guest.setLastName("Doe");
        guest.setPhone("+1234567890");
        guest.setEmail("john.doe@email.com");
        guest.setSeatingPreference("Window table");
        guest.setDietaryRestrictions(Arrays.asList("Vegetarian", "No nuts"));
        guest.setFavoriteDrinks(Arrays.asList("Red wine", "Sparkling water"));
        guest.setBirthday(LocalDate.of(1985, 6, 15));
        guest.setAnniversary(LocalDate.of(2010, 9, 20));
        guest.setNotes("Prefers quiet atmosphere");
        return guest;
    }

    public static Guest createGuestWithUpcomingBirthday() {
        Guest guest = createBasicGuest();
        guest.setFirstName("Birthday");
        guest.setLastName("Guest");
        guest.setPhone("+1234567891");
        guest.setEmail("birthday@email.com");
        // Set birthday to next week
        guest.setBirthday(LocalDate.now().plusDays(7));
        return guest;
    }

    public static Guest createVeganGuest() {
        Guest guest = createBasicGuest();
        guest.setFirstName("Vegan");
        guest.setLastName("Customer");
        guest.setPhone("+1234567892");
        guest.setEmail("vegan@email.com");
        guest.setDietaryRestrictions(Arrays.asList("Vegan", "Gluten-free"));
        guest.setFavoriteDrinks(Arrays.asList("Kombucha", "Green tea"));
        return guest;
    }

    // Visit builders
    public static Visit createBasicVisit(Guest guest, Staff staff) {
        Visit visit = new Visit();
        visit.setGuest(guest);
        visit.setStaff(staff);
        visit.setVisitDate(LocalDate.now());
        visit.setVisitTime(LocalTime.of(19, 30));
        visit.setPartySize(2);
        visit.setTableNumber("A5");
        visit.setServiceNotes("Great service, enjoyed the meal");
        return visit;
    }

    public static Visit createVisitWithSpecialNotes(Guest guest, Staff staff) {
        Visit visit = createBasicVisit(guest, staff);
        visit.setVisitDate(LocalDate.now().minusDays(1));
        visit.setServiceNotes("Celebrated anniversary, provided complimentary dessert");
        return visit;
    }

    // DTO builders for requests
    public static LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    public static GuestCreateRequest createGuestCreateRequest() {
        GuestCreateRequest request = new GuestCreateRequest();
        request.setFirstName("New");
        request.setLastName("Guest");
        request.setPhone("+1234567893");
        request.setEmail("new.guest@email.com");
        request.setSeatingPreference("Booth");
        request.setDietaryRestrictions(Arrays.asList("No seafood"));
        request.setFavoriteDrinks(Arrays.asList("Beer", "Cocktails"));
        request.setBirthday(LocalDate.of(1990, 3, 15));
        request.setNotes("First time visitor");
        return request;
    }

    public static GuestUpdateRequest createGuestUpdateRequest() {
        GuestUpdateRequest request = new GuestUpdateRequest();
        request.setFirstName("Updated");
        request.setLastName("Guest");
        request.setPhone("+1234567894");
        request.setEmail("updated.guest@email.com");
        request.setSeatingPreference("Patio");
        request.setDietaryRestrictions(Arrays.asList("Keto"));
        request.setFavoriteDrinks(Arrays.asList("Wine", "Water"));
        request.setAnniversary(LocalDate.of(2015, 8, 10));
        request.setNotes("Updated preferences");
        return request;
    }

    public static VisitCreateRequest createVisitCreateRequest(Long guestId, Long staffId) {
        VisitCreateRequest request = new VisitCreateRequest();
        request.setGuestId(guestId);
        request.setStaffId(staffId);
        request.setVisitDate(LocalDate.now());
        request.setVisitTime(LocalTime.of(20, 0));
        request.setPartySize(4);
        request.setTableNumber("B3");
        request.setServiceNotes("Excellent service, will return");
        return request;
    }

    public static VisitUpdateRequest createVisitUpdateRequest() {
        VisitUpdateRequest request = new VisitUpdateRequest();
        request.setVisitDate(LocalDate.now().minusDays(1));
        request.setVisitTime(LocalTime.of(18, 30));
        request.setPartySize(2);
        request.setTableNumber("C1");
        request.setServiceNotes("Updated visit information");
        return request;
    }

    public static GuestSearchRequest createGuestSearchRequest() {
        GuestSearchRequest request = new GuestSearchRequest();
        request.setSearchTerm("John");
        request.setPage(0);
        request.setSize(20);
        request.setSortBy("firstName");
        request.setSortDirection("ASC");
        return request;
    }

    public static GuestSearchRequest createDietaryFilterRequest(List<String> restrictions) {
        GuestSearchRequest request = new GuestSearchRequest();
        request.setDietaryRestrictions(restrictions);
        request.setPage(0);
        request.setSize(20);
        request.setSortBy("firstName");
        request.setSortDirection("ASC");
        return request;
    }

    public static GuestSearchRequest createUpcomingOccasionsRequest() {
        GuestSearchRequest request = new GuestSearchRequest();
        request.setUpcomingOccasions(true);
        request.setPage(0);
        request.setSize(20);
        request.setSortBy("firstName");
        request.setSortDirection("ASC");
        return request;
    }
}