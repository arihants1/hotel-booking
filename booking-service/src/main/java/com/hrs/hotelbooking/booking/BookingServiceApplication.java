package com.hrs.hotelbooking.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * HRS Booking Service Application
 * Microservice responsible for booking management and reservation operations
 * Handles complete booking lifecycle from creation to completion
 * Part of the HRS hotel booking system ecosystem
 *
 * @author arihants1
 */
@SpringBootApplication(scanBasePackages = {"com.hrs.hotelbooking.booking", "com.hrs.hotelbooking.shared"})
@EnableCaching
@EnableTransactionManagement
public class BookingServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(BookingServiceApplication.class, args);
    }
}