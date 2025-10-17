package com.restaurant.vip.dto;

import com.restaurant.vip.validation.NoSqlInjection;
import com.restaurant.vip.validation.SafeHtml;
import com.restaurant.vip.validation.ValidPhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class GuestUpdateRequest {
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @SafeHtml(maxLength = 100)
    @NoSqlInjection
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @SafeHtml(maxLength = 100)
    @NoSqlInjection
    private String lastName;
    
    @NotBlank(message = "Phone number is required")
    @ValidPhoneNumber
    private String phone;
    
    @Email(message = "Email should be valid")
    @SafeHtml(maxLength = 255)
    @NoSqlInjection
    private String email;
    
    @Size(max = 100, message = "Seating preference must not exceed 100 characters")
    @SafeHtml(maxLength = 100)
    @NoSqlInjection
    private String seatingPreference;
    
    private List<String> dietaryRestrictions;
    
    private List<String> favoriteDrinks;
    
    private LocalDate birthday;
    
    private LocalDate anniversary;
    
    @SafeHtml(maxLength = 1000, allowBasicFormatting = true)
    @NoSqlInjection
    private String notes;
    
    // Constructors
    public GuestUpdateRequest() {}
    
    // Getters and Setters
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
}