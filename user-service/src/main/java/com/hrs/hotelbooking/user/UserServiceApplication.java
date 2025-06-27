package com.hrs.hotelbooking.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * HRS User Service Application - Essential Features
 * Microservice for user management in the HRS booking system
 * 
 * @author arihants1
 */
@SpringBootApplication(scanBasePackages = {"com.hrs.hotelbooking.user", "com.hrs.hotelbooking.shared"})
@EnableCaching
@EnableTransactionManagement
public class UserServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(UserServiceApplication.class, args);
    }
}