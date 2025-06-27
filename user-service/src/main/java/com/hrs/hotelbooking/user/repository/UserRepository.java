package com.hrs.hotelbooking.user.repository;

import com.hrs.hotelbooking.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * HRS User Repository - Essential Operations Only
 * Data access for user operations in the HRS booking system
 * 
 * @author arihants1*
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (case insensitive)
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if email exists (case insensitive)
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find active users with pagination
     */
    Page<User> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find users by name pattern (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "ORDER BY u.firstName ASC, u.lastName ASC")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find users by email pattern (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')) " +
           "ORDER BY u.email ASC")
    List<User> findByEmailContainingIgnoreCase(@Param("email") String email);

    /**
     * Count active users
     */
    long countByIsActiveTrue();

    /**
     * Search users by multiple criteria
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(:name IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "ORDER BY u.createdAt DESC")
    Page<User> searchUsers(@Param("name") String name, @Param("email") String email, Pageable pageable);
}