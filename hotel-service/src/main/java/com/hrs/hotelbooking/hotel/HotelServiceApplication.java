package com.hrs.hotelbooking.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * HRS Hotel Service Application
 * Microservice responsible for hotel data management and search functionality
 * Handles hotel information, availability checks, and search operations
 * Part of the HRS hotel booking system ecosystem
 * 
 * @author arihants1
 */
@SpringBootApplication(scanBasePackages = {"com.hrs.hotelbooking.hotel", "com.hrs.hotelbooking.shared"})
@EnableCaching
@EnableTransactionManagement
public class HotelServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelServiceApplication.class, args);
    }
}