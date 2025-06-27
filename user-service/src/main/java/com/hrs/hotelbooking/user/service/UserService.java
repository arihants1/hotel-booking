package com.hrs.hotelbooking.user.service;

import com.hrs.hotelbooking.shared.dto.UserDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * HRS User Service Interface - Essential Features Only
 * Core user operations for the HRS booking system
 *
 * @author arihants1
 */
public interface UserService {

    /**
     * Create new user
     */
    UserDTO createUser(UserDTO userDTO);

    /**
     * Get user by ID
     */
    UserDTO getUserById(Long id);

    /**
     * Get user by email
     */
    UserDTO getUserByEmail(String email);

    /**
     * Update user
     */
    UserDTO updateUser(Long id, UserDTO userDTO);

    /**
     * Delete user (soft delete)
     */
    void deleteUser(Long id);

    /**
     * Search users by name
     */
    List<UserDTO> searchUsersByName(String name);

    /**
     * Search users by email pattern
     */
    List<UserDTO> searchUsersByEmail(String email);

    /**
     * Get user count
     */
    Long getUserCount();
}