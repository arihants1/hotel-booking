package com.hrs.hotelbooking.hotel.repository;

import com.hrs.hotelbooking.hotel.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * HRS Hotel Repository - Essential Operations Only
 * Data access for hotel operations in the HRS booking system
 * Fixed parameter mapping issues
 *
 * @author arihants1
 * @since 2025-06-27 11:38:16 UTC
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Find active hotels with pagination
     */
    Page<Hotel> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find active hotels by city with case-insensitive search
     */
    Page<Hotel> findByCityIgnoreCaseAndIsActiveTrueOrderByStarRatingDescBasePriceAsc(String city, Pageable pageable);

    /**
     * Comprehensive hotel search - FIXED METHOD
     * All parameters now match the query
     */
    @Query("SELECT h FROM Hotel h WHERE h.isActive = true AND " +
            "(:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
            "(:country IS NULL OR LOWER(h.country) LIKE LOWER(CONCAT('%', :country, '%'))) AND " +
            "(:minStarRating IS NULL OR h.starRating >= :minStarRating) AND " +
            "(:maxStarRating IS NULL OR h.starRating <= :maxStarRating) AND " +
            "(:minPrice IS NULL OR h.basePrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR h.basePrice <= :maxPrice) " +
            "ORDER BY h.starRating DESC, h.basePrice ASC")
    Page<Hotel> searchHotels(@Param("city") String city,
                             @Param("country") String country,
                             @Param("minStarRating") Integer minStarRating,
                             @Param("maxStarRating") Integer maxStarRating,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             Pageable pageable);

}