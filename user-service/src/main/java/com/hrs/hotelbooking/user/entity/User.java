package com.hrs.hotelbooking.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * HRS User Entity - Essential Fields Only
 * Represents user information in the HRS booking system database
 * 
 * @author arihants1
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_active", columnList = "is_active"),
    @Index(name = "idx_user_created", columnList = "created_at"),
    @Index(name = "idx_user_name", columnList = "first_name, last_name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    @Builder.Default
    private String createdBy = "arihants1";

    @Column(name = "updated_by", length = 50)
    @Builder.Default
    private String updatedBy = "arihants1";

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (createdBy == null) {
            createdBy = "arihants1";
        }
        updatedBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updatedBy = "arihants1";
    }

    /**
     * Get full name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "Unknown User";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        String fullName = getFullName();
        if ("Unknown User".equals(fullName)) {
            return "User " + id;
        }
        return fullName;
    }

    /**
     * Check if user has complete profile
     */
    public boolean hasCompleteProfile() {
        return firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty();
    }
}