package com.restaurant.vip.exception;

public class DuplicateGuestException extends RuntimeException {
    
    private final String phone;
    private final Long existingGuestId;
    
    public DuplicateGuestException(String phone, Long existingGuestId) {
        super("Guest with phone number " + phone + " already exists");
        this.phone = phone;
        this.existingGuestId = existingGuestId;
    }
    
    public DuplicateGuestException(String phone, Long existingGuestId, String message) {
        super(message);
        this.phone = phone;
        this.existingGuestId = existingGuestId;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public Long getExistingGuestId() {
        return existingGuestId;
    }
}