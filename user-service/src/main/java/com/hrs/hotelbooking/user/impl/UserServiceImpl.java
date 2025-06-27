package com.hrs.hotelbooking.user.impl;

import com.hrs.hotelbooking.user.entity.User;
import com.hrs.hotelbooking.user.mapper.UserMapper;
import com.hrs.hotelbooking.user.repository.UserRepository;
import com.hrs.hotelbooking.user.service.UserService;
import com.hrs.hotelbooking.shared.dto.UserDTO;
import com.hrs.hotelbooking.shared.exception.ResourceNotFoundException;
import com.hrs.hotelbooking.shared.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * HRS User Service Implementation - Essential Features Only
 * Core business logic for user operations in the HRS booking system
 *
 * @author arihants1
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final String CURRENT_USER = "arihants1";

    @Override
    @Transactional
    @CacheEvict(value = {"users", "usersByEmail"}, allEntries = true)
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating HRS user: {} by {}", userDTO.getEmail(), CURRENT_USER);

        // Validate user data
        validateUserDto(userDTO);

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(userDTO.getEmail())) {
            throw new BusinessValidationException("Email already exists: " + userDTO.getEmail());
        }

        // Create and save user
        User user = userMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);

        UserDTO result = userMapper.toDto(savedUser);
        log.info("Successfully created HRS user with ID: {} ", savedUser.getId());

        return result;
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(Long id) {
        log.info("Fetching HRS user with ID: {} by {}", id, CURRENT_USER);

        validateUserId(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getIsActive()) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        UserDTO result = userMapper.toDto(user);
        log.debug("Successfully retrieved HRS user: {} ", result.getEmail());

        return result;
    }

    @Override
    @Cacheable(value = "usersByEmail", key = "#email")
    public UserDTO getUserByEmail(String email) {
        log.info("Fetching HRS user by email: {} by {}", email, CURRENT_USER);

        validateEmail(email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!user.getIsActive()) {
            throw new ResourceNotFoundException("User", "email", email);
        }

        UserDTO result = userMapper.toDto(user);
        log.debug("Successfully retrieved HRS user by email");
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "usersByEmail"}, allEntries = true)
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating HRS user with ID: {} by {}", id, CURRENT_USER);

        validateUserId(id);
        validateUserDto(userDTO);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!existingUser.getIsActive()) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        // Update user
        userMapper.updateEntityFromDto(existingUser, userDTO);
        User updatedUser = userRepository.save(existingUser);

        UserDTO result = userMapper.toDto(updatedUser);
        log.info("Successfully updated HRS user: {} ", result.getEmail());

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "usersByEmail"}, allEntries = true)
    public void deleteUser(Long id) {
        log.info("Deleting HRS user with ID: {} by {}", id, CURRENT_USER);

        validateUserId(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getIsActive()) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        // Soft delete
        user.setIsActive(false);
        user.setUpdatedBy(CURRENT_USER);
        userRepository.save(user);

        log.info("Successfully soft-deleted HRS user with ID: {} ", id);
    }


    @Override
    public List<UserDTO> searchUsersByName(String name) {
        log.info("Searching HRS users by name: {} by {}", name, CURRENT_USER);

        if (!StringUtils.hasText(name)) {
            throw new BusinessValidationException("Search name cannot be empty");
        }

        List<User> users = userRepository.findByNameContainingIgnoreCase(name);
        List<UserDTO> result = userMapper.toDtoList(users);

        log.debug("Found {} HRS users matching name: {} ", result.size(), name);

        return result;
    }

    @Override
    public List<UserDTO> searchUsersByEmail(String email) {
        log.info("Searching HRS users by email pattern: {} by {}", email, CURRENT_USER);

        if (!StringUtils.hasText(email)) {
            throw new BusinessValidationException("Search email cannot be empty");
        }

        List<User> users = userRepository.findByEmailContainingIgnoreCase(email);
        List<UserDTO> result = userMapper.toDtoList(users);

        log.debug("Found {} HRS users matching email pattern: {} ", result.size(), email);

        return result;
    }

    @Override
    public Long getUserCount() {
        log.info("Fetching HRS user count by {}", CURRENT_USER);

        Long count = userRepository.countByIsActiveTrue();

        log.debug("Total active HRS users: {} ", count);

        return count;
    }

    // Private validation methods

    private void validateUserDto(UserDTO userDTO) {
        if (userDTO == null) {
            throw new BusinessValidationException("User information is required");
        }
        if (!StringUtils.hasText(userDTO.getFirstName())) {
            throw new BusinessValidationException("First name is required");
        }
        if (!StringUtils.hasText(userDTO.getLastName())) {
            throw new BusinessValidationException("Last name is required");
        }
        if (!StringUtils.hasText(userDTO.getEmail())) {
            throw new BusinessValidationException("Email is required");
        }
        if (!isValidEmail(userDTO.getEmail())) {
            throw new BusinessValidationException("Invalid email format");
        }
        if (userDTO.getPhone() != null && !isValidPhone(userDTO.getPhone())) {
            throw new BusinessValidationException("Invalid phone format");
        }
    }

    private void validateUserId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessValidationException("Invalid user ID provided");
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessValidationException("Email is required");
        }
        if (!isValidEmail(email)) {
            throw new BusinessValidationException("Invalid email format");
        }
    }

    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new BusinessValidationException("Page number cannot be negative");
        }
        if (size <= 0 || size > 50) {
            throw new BusinessValidationException("Page size must be between 1 and 50");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return false;

        // Remove common formatting characters
        String normalized = phone.replaceAll("[\\s\\-()]", "");

        // Check if it matches E.164 format after normalization
        return normalized.matches("^\\+?[1-9]\\d{9,14}$");
    }
}