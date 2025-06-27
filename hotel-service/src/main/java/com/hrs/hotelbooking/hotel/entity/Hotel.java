package com.hrs.hotelbooking.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * HRS Hotel Entity - Essential Fields Only
 * Simplified hotel representation for core operations
 *
 * @author arihants1
 */
@Entity
@Table(name = "hotels", indexes = {
        @Index(name = "idx_hotel_location", columnList = "city, country"),
        @Index(name = "idx_hotel_price", columnList = "base_price"),
        @Index(name = "idx_hotel_rating", columnList = "star_rating"),
        @Index(name = "idx_hotel_created", columnList = "created_at"),
        @Index(name = "idx_hotel_name", columnList = "name"),
        @Index(name = "idx_hotel_active", columnList = "is_active"),
        @Index(name = "idx_hotel_search", columnList = "city, star_rating, base_price")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", nullable = false, length = 500)
    private String location;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "star_rating")
    private Integer starRating;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "amenities", columnDefinition = "jsonb")
    private Map<String, Object> amenities;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "total_rooms")
    private Integer totalRooms;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
}