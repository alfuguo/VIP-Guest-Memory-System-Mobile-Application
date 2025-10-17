package com.restaurant.vip.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "guests")
@Where(clause = "deleted_at IS NULL") // Soft delete filter
public class Guest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phone;
    
    @Column(length = 255)
    @Email(message = "Email should be valid")
    private String email;
    
    @Column(name = "photo_url", length = 500)
    private String photoUrl;
    
    @Column(name = "seating_preference", length = 100)
    @Size(max = 100, message = "Seating preference must not exceed 100 characters")
    private String seatingPreference;
    
    @ElementCollection
    @CollectionTable(name = "guest_dietary_restrictions", joinColumns = @JoinColumn(name = "guest_id"))
    @Column(name = "restriction")
    private List<String> dietaryRestrictions;
    
    @ElementCollection
    @CollectionTable(name = "guest_favorite_drinks", joinColumns = @JoinColumn(name = "guest_id"))
    @Column(name = "drink")
    private List<String> favoriteDrinks;
    
    @Column
    private LocalDate birthday;
    
    @Column
    private LocalDate anniversary;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Staff createdBy;
    
    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("visitDate DESC, visitTime DESC")
    private List<Visit> visits;
    
    // Constructors
    public Guest() {}
    
    public Guest(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
    
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
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
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
    
    public Staff getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Staff createdBy) {
        this.createdBy = createdBy;
    }
    
    public List<Visit> getVisits() {
        return visits;
    }
    
    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }
    
    // Utility methods
    public String getFullName() {
        if (lastName != null && !lastName.trim().isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    
    public void restore() {
        this.deletedAt = null;
    }
    
    public Visit getLastVisit() {
        return visits != null && !visits.isEmpty() ? visits.get(0) : null;
    }
    
    public int getVisitCount() {
        return visits != null ? visits.size() : 0;
    }
    
    public boolean hasBirthdayThisMonth() {
        if (birthday == null) return false;
        LocalDate now = LocalDate.now();
        return birthday.getMonth() == now.getMonth();
    }
    
    public boolean hasAnniversaryThisMonth() {
        if (anniversary == null) return false;
        LocalDate now = LocalDate.now();
        return anniversary.getMonth() == now.getMonth();
    }
}