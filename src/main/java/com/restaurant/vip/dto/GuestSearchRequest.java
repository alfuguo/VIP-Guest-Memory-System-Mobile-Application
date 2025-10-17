package com.restaurant.vip.dto;

import java.util.List;

public class GuestSearchRequest {
    
    private String searchTerm;
    private String seatingPreference;
    private List<String> dietaryRestrictions;
    private List<String> favoriteDrinks;
    private Boolean hasBirthday;
    private Boolean hasAnniversary;
    private Boolean upcomingOccasions; // Next 30 days
    private int page = 0;
    private int size = 20;
    private String sortBy = "firstName";
    private String sortDirection = "ASC";
    
    // Constructors
    public GuestSearchRequest() {}
    
    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }
    
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
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
    
    public Boolean getHasBirthday() {
        return hasBirthday;
    }
    
    public void setHasBirthday(Boolean hasBirthday) {
        this.hasBirthday = hasBirthday;
    }
    
    public Boolean getHasAnniversary() {
        return hasAnniversary;
    }
    
    public void setHasAnniversary(Boolean hasAnniversary) {
        this.hasAnniversary = hasAnniversary;
    }
    
    public Boolean getUpcomingOccasions() {
        return upcomingOccasions;
    }
    
    public void setUpcomingOccasions(Boolean upcomingOccasions) {
        this.upcomingOccasions = upcomingOccasions;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = Math.max(0, page);
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = Math.min(Math.max(1, size), 100); // Limit between 1 and 100
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}