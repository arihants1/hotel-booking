package com.hrs.hotelbooking.user.mapper;

import com.hrs.hotelbooking.user.entity.User;
import com.hrs.hotelbooking.shared.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HRS User Mapper - Essential Mappings Only
 * Converts between User entity and UserDTO
 * 
 * @author arihants1
 */
@Component
@Slf4j
public class UserMapper {

    /**
     * Convert User entity to UserDTO
     */
    public UserDTO toDto(User user) {
        if (user == null) {
            log.warn("Attempting to convert null User entity to DTO");
            return null;
        }

        try {
            return UserDTO.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .fullName(user.getFullName())
                    .displayName(user.getDisplayName())
                    .isActive(user.getIsActive())
                    .hasCompleteProfile(user.hasCompleteProfile())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting User entity {} to DTO: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to convert User to DTO", e);
        }
    }

    /**
     * Convert UserDTO to User entity
     */
    public User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            log.warn("Attempting to convert null UserDTO to entity");
            return null;
        }

        try {
            return User.builder()
                    .id(userDTO.getId())
                    .firstName(userDTO.getFirstName())
                    .lastName(userDTO.getLastName())
                    .email(userDTO.getEmail())
                    .phone(userDTO.getPhone())
                    .isActive(userDTO.getIsActive() != null ? userDTO.getIsActive() : true)
                    .createdAt(userDTO.getCreatedAt())
                    .updatedAt(userDTO.getUpdatedAt())
                    .createdBy("arihants1")
                    .updatedBy("arihants1")
                    .build();
        } catch (Exception e) {
            log.error("Error converting UserDTO {} to entity: {}", userDTO.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to convert UserDTO to entity", e);
        }
    }

    /**
     * Convert list of User entities to UserDTO list
     */
    public List<UserDTO> toDtoList(List<User> users) {
        if (users == null || users.isEmpty()) {
            log.debug("Converting empty or null user list to DTOs");
            return List.of();
        }

        try {
            List<UserDTO> result = users.stream()
                    .map(this::toDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
            
            log.debug("Successfully converted {} users to DTOs", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error converting user list to DTOs: {}", e.getMessage());
            throw new RuntimeException("Failed to convert user list to DTOs", e);
        }
    }

    /**
     * Update existing User entity with UserDTO data
     */
    public void updateEntityFromDto(User existingUser, UserDTO userDTO) {
        if (existingUser == null || userDTO == null) {
            log.warn("Cannot update user entity: user or DTO is null");
            return;
        }

        try {
            if (userDTO.getFirstName() != null) {
                existingUser.setFirstName(userDTO.getFirstName());
            }
            if (userDTO.getLastName() != null) {
                existingUser.setLastName(userDTO.getLastName());
            }
            if (userDTO.getEmail() != null) {
                existingUser.setEmail(userDTO.getEmail());
            }
            if (userDTO.getPhone() != null) {
                existingUser.setPhone(userDTO.getPhone());
            }
            
            // Always update the updatedBy field
            existingUser.setUpdatedBy("arihants1");
            
            log.debug("Successfully updated user entity {} from DTO", existingUser.getId());
        } catch (Exception e) {
            log.error("Error updating user entity {} from DTO: {}", existingUser.getId(), e.getMessage());
            throw new RuntimeException("Failed to update user entity from DTO", e);
        }
    }

    /**
     * Create user summary DTO (lightweight version)
     */
    public UserDTO toSummaryDto(User user) {
        if (user == null) {
            return null;
        }

        try {
            return UserDTO.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .displayName(user.getDisplayName())
                    .isActive(user.getIsActive())
                    .build();
        } catch (Exception e) {
            log.error("Error converting User entity {} to summary DTO: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to convert User to summary DTO", e);
        }
    }
}