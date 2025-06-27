package com.hrs.hotelbooking.shared.dto;

import lombok.Getter;

/**
 * HRS Booking Status Enumeration
 * Represents the lifecycle states of a hotel booking in the HRS system
 * 
 * @author arihants1
 */
@Getter
public enum BookingStatus {
    PENDING("Booking is pending confirmation"),
    CONFIRMED("Booking is confirmed"), 
    CHECKED_IN("Guest has checked in"),
    CHECKED_OUT("Guest has checked out"),
    CANCELLED("Booking has been cancelled"),
    NO_SHOW("Guest did not show up");
    
    private final String description;
    
    BookingStatus(String description) {
        this.description = description;
    }

}