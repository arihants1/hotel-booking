package com.hrs.hotelbooking.user.controller;

import com.hrs.hotelbooking.user.service.UserService;
import com.hrs.hotelbooking.shared.dto.UserDTO;
import com.hrs.hotelbooking.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

/**
 * HRS User Controller - Essential Features Only
 * REST API endpoints for user management in the HRS booking system
 *
 * @author arihants1
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "HRS User API", description = "Essential user management operations for HRS booking system")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    /**
     * Create new user
     */
    @PostMapping
    @Operation(summary = "Create HRS user", 
               description = "Create a new user with validation")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Parameter(description = "User information", required = true)
            @Valid @RequestBody UserDTO userDTO) {

        log.info("Creating new HRS user: {} ", userDTO.getEmail());

        UserDTO createdUser = userService.createUser(userDTO);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User created successfully"));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get HRS user details", 
               description = "Retrieve detailed user information by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @Parameter(description = "User ID", required = true) 
            @PathVariable @Min(value = 1, message = "User ID must be positive") Long id) {

        log.info("Fetching HRS user details for ID: {} ", id);

        UserDTO user = userService.getUserById(id);
        
        return ResponseEntity.ok(ApiResponse.success(user, "User details retrieved successfully"));
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get HRS user by email", 
               description = "Retrieve user information by email address")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(
            @Parameter(description = "Email address", required = true) 
            @PathVariable String email) {

        log.info("Fetching HRS user by email: {} ", email);

        UserDTO user = userService.getUserByEmail(email);
        
        return ResponseEntity.ok(ApiResponse.success(user, "User found by email successfully"));
    }

    /**
     * Update existing user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update HRS user", 
               description = "Update an existing user with validation")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @Parameter(description = "User ID", required = true) 
            @PathVariable @Min(value = 1, message = "User ID must be positive") Long id,
            
            @Parameter(description = "Updated user information", required = true)
            @Valid @RequestBody UserDTO userDTO) {

        log.info("Updating HRS user with ID: {} ", id);

        UserDTO updatedUser = userService.updateUser(id, userDTO);
        
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    /**
     * Delete user (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete HRS user", 
               description = "Soft delete a user (marks as inactive)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID", required = true) 
            @PathVariable @Min(value = 1, message = "User ID must be positive") Long id) {

        log.info("Deleting HRS user with ID: {} ", id);

        userService.deleteUser(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    /**
     * Search users by name
     */
    @GetMapping("/search/name")
    @Operation(summary = "Search users by name", 
               description = "Search users by first name or last name")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsersByName(
            @Parameter(description = "Name to search for", required = true)
            @RequestParam String name) {

        log.info("Searching HRS users by name: {}", name);

        List<UserDTO> users = userService.searchUsersByName(name);
        
        return ResponseEntity.ok(ApiResponse.success(users,
                String.format("Found %d users matching name: %s", users.size(), name)));
    }

    /**
     * Search users by email pattern
     */
    @GetMapping("/search/email")
    @Operation(summary = "Search users by email", 
               description = "Search users by email pattern")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsersByEmail(
            @Parameter(description = "Email pattern to search for", required = true)
            @RequestParam String email) {

        log.info("Searching HRS users by email: {} by arihants1", email);

        List<UserDTO> users = userService.searchUsersByEmail(email);
        
        return ResponseEntity.ok(ApiResponse.success(users,
                String.format("Found %d users matching email pattern: %s", users.size(), email)));
    }

    /**
     * Get user count
     */
    @GetMapping("/count")
    @Operation(summary = "Get user count", 
               description = "Get total count of active users")
    public ResponseEntity<ApiResponse<Long>> getUserCount() {
        log.info("Fetching HRS user count by arihants1");

        Long count = userService.getUserCount();
        
        return ResponseEntity.ok(ApiResponse.success(count, "User count retrieved successfully"));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "User service health check", 
               description = "Check if user service is running properly")
    public ResponseEntity<ApiResponse<HealthCheckDTO>> healthCheck() {
        log.debug("User service health check at 2025-06-27 06:05:10 by arihants1");
        
        HealthCheckDTO health = HealthCheckDTO.builder()
                .status("UP")
                .service("hrs-user-service")
                .version("1.0.0")
                .timestamp(java.time.LocalDateTime.now())
                .message("HRS User Service is running")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(health, "Service is healthy"));
    }

    /**
     * Health Check DTO for internal use
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HealthCheckDTO {
        private String status;
        private String service;
        private String version;
        private java.time.LocalDateTime timestamp;
        private String message;
    }
}