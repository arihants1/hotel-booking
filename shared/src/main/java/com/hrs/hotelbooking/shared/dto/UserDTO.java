package com.hrs.hotelbooking.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * HRS User Data Transfer Object - Essential Fields Only
 * Represents user information for the HRS booking system
 *
 * @author arihants1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information for HRS system")
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID", example = "1")
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone must be valid")
    @Schema(description = "Phone number", example = "+1-555-0123")
    private String phone;

    @Schema(description = "Full name (computed)", example = "John Doe")
    private String fullName;

    @Schema(description = "Display name (computed)", example = "John Doe")
    private String displayName;

    @Schema(description = "Whether user is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Whether user has complete profile", example = "true")
    private Boolean hasCompleteProfile;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Creation timestamp", example = "2025-06-27 06:05:10")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last update timestamp", example = "2025-06-27 06:05:10")
    private LocalDateTime updatedAt;

    // Helper methods

    /**
     * Get initials
     */
    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    /**
     * Check if user has contact info
     */
    public boolean hasContactInfo() {
        return (email != null && !email.trim().isEmpty()) ||
                (phone != null && !phone.trim().isEmpty());
    }
}