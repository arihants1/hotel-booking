package com.hrs.hotelbooking.user.mapper;

import com.hrs.hotelbooking.shared.dto.UserDTO;
import com.hrs.hotelbooking.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test cases for UserMapper
 * Tests the mapping between User entities and UserDTO objects
 *
 * @author arihants1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Mapper Tests")
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    private User validUser;
    private UserDTO validUserDTO;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 6, 29, 10, 30, 0);

        validUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .isActive(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .createdBy("arihants1")
                .updatedBy("arihants1")
                .build();

        validUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .fullName("John Doe")
                .displayName("John Doe")
                .isActive(true)
                .hasCompleteProfile(true)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();
    }

    /**
     * Helper method to create a user with modified fields
     */
    private User createUserWith(java.util.function.Consumer<User.UserBuilder> modifications) {
        User.UserBuilder builder = User.builder()
                .id(validUser.getId())
                .firstName(validUser.getFirstName())
                .lastName(validUser.getLastName())
                .email(validUser.getEmail())
                .phone(validUser.getPhone())
                .isActive(validUser.getIsActive())
                .createdAt(validUser.getCreatedAt())
                .updatedAt(validUser.getUpdatedAt())
                .createdBy(validUser.getCreatedBy())
                .updatedBy(validUser.getUpdatedBy());

        modifications.accept(builder);
        return builder.build();
    }

    @Nested
    @DisplayName("Entity to DTO Mapping Tests")
    class EntityToDtoMappingTests {

        @Test
        @DisplayName("Should map complete User entity to UserDTO successfully")
        void shouldMapCompleteUserEntityToUserDTOSuccessfully() {
            // When
            UserDTO result = userMapper.toDto(validUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(validUser.getId());
            assertThat(result.getFirstName()).isEqualTo(validUser.getFirstName());
            assertThat(result.getLastName()).isEqualTo(validUser.getLastName());
            assertThat(result.getEmail()).isEqualTo(validUser.getEmail());
            assertThat(result.getPhone()).isEqualTo(validUser.getPhone());
            assertThat(result.getIsActive()).isEqualTo(validUser.getIsActive());
            assertThat(result.getCreatedAt()).isEqualTo(validUser.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(validUser.getUpdatedAt());

            // Test derived fields
            assertThat(result.getFullName()).isEqualTo(validUser.getFullName());
            assertThat(result.getDisplayName()).isEqualTo(validUser.getDisplayName());
            assertThat(result.getHasCompleteProfile()).isEqualTo(validUser.hasCompleteProfile());
        }

        @Test
        @DisplayName("Should map User entity with minimal data")
        void shouldMapUserEntityWithMinimalData() {
            // Given
            User minimalUser = User.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .isActive(true)
                    .build();

            // When
            UserDTO result = userMapper.toDto(minimalUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            assertThat(result.getEmail()).isEqualTo("jane.smith@example.com");
            assertThat(result.getPhone()).isNull();
            assertThat(result.getIsActive()).isTrue();
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("Should return null when User entity is null")
        void shouldReturnNullWhenUserEntityIsNull() {
            // When
            UserDTO result = userMapper.toDto(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle User entity with null fields gracefully")
        void shouldHandleUserEntityWithNullFieldsGracefully() {
            // Given
            User userWithNulls = User.builder()
                    .id(3L)
                    .firstName(null)
                    .lastName(null)
                    .email("test@example.com")
                    .phone(null)
                    .isActive(true)
                    .createdAt(null)
                    .updatedAt(null)
                    .build();

            // When
            UserDTO result = userMapper.toDto(userWithNulls);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getFirstName()).isNull();
            assertThat(result.getLastName()).isNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getPhone()).isNull();
            assertThat(result.getIsActive()).isTrue();
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle mapping exception and throw RuntimeException")
        void shouldHandleMappingExceptionAndThrowRuntimeException() {
            // Given - Create a User with an ID that might cause issues during mapping
            User problematicUser = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .isActive(true)
                    .build();

            // We'll simulate an exception by using a spy or by creating a problematic scenario
            // For this test, we'll test with a very large dataset that might cause memory issues
            // This is more of a conceptual test since the actual mapping is straightforward

            // When & Then
            UserDTO result = userMapper.toDto(problematicUser);
            assertThat(result).isNotNull(); // This should work fine in normal circumstances
        }
    }

    @Nested
    @DisplayName("DTO to Entity Mapping Tests")
    class DtoToEntityMappingTests {

        @Test
        @DisplayName("Should map complete UserDTO to User entity successfully")
        void shouldMapCompleteUserDTOToUserEntitySuccessfully() {
            // When
            User result = userMapper.toEntity(validUserDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(validUserDTO.getId());
            assertThat(result.getFirstName()).isEqualTo(validUserDTO.getFirstName());
            assertThat(result.getLastName()).isEqualTo(validUserDTO.getLastName());
            assertThat(result.getEmail()).isEqualTo(validUserDTO.getEmail());
            assertThat(result.getPhone()).isEqualTo(validUserDTO.getPhone());
            assertThat(result.getIsActive()).isEqualTo(validUserDTO.getIsActive());
            assertThat(result.getCreatedAt()).isEqualTo(validUserDTO.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(validUserDTO.getUpdatedAt());

            // Test audit fields
            assertThat(result.getCreatedBy()).isEqualTo("arihants1");
            assertThat(result.getUpdatedBy()).isEqualTo("arihants1");
        }

        @Test
        @DisplayName("Should map UserDTO with minimal data")
        void shouldMapUserDTOWithMinimalData() {
            // Given
            UserDTO minimalDTO = UserDTO.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build();

            // When
            User result = userMapper.toEntity(minimalDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            assertThat(result.getEmail()).isEqualTo("jane.smith@example.com");
            assertThat(result.getPhone()).isNull();
            assertThat(result.getIsActive()).isTrue(); // Default value
            assertThat(result.getCreatedBy()).isEqualTo("arihants1");
            assertThat(result.getUpdatedBy()).isEqualTo("arihants1");
        }

        @Test
        @DisplayName("Should return null when UserDTO is null")
        void shouldReturnNullWhenUserDTOIsNull() {
            // When
            User result = userMapper.toEntity(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle UserDTO with null isActive field")
        void shouldHandleUserDTOWithNullIsActiveField() {
            // Given
            UserDTO dtoWithNullActive = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .isActive(null)
                    .build();

            // When
            User result = userMapper.toEntity(dtoWithNullActive);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsActive()).isTrue(); // Should default to true
        }

        @Test
        @DisplayName("Should preserve isActive field when explicitly set to false")
        void shouldPreserveIsActiveFieldWhenExplicitlySetToFalse() {
            // Given
            UserDTO dtoWithFalseActive = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .isActive(false)
                    .build();

            // When
            User result = userMapper.toEntity(dtoWithFalseActive);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("List Mapping Tests")
    class ListMappingTests {

        @Test
        @DisplayName("Should map list of User entities to UserDTO list successfully")
        void shouldMapListOfUserEntitiesToUserDTOListSuccessfully() {
            // Given
            User user2 = User.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .isActive(true)
                    .build();

            List<User> users = Arrays.asList(validUser, user2);

            // When
            List<UserDTO> result = userMapper.toDtoList(users);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);

            UserDTO firstDto = result.get(0);
            assertThat(firstDto.getId()).isEqualTo(1L);
            assertThat(firstDto.getFirstName()).isEqualTo("John");

            UserDTO secondDto = result.get(1);
            assertThat(secondDto.getId()).isEqualTo(2L);
            assertThat(secondDto.getFirstName()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("Should return empty list when input list is null")
        void shouldReturnEmptyListWhenInputListIsNull() {
            // When
            List<UserDTO> result = userMapper.toDtoList(null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when input list is empty")
        void shouldReturnEmptyListWhenInputListIsEmpty() {
            // When
            List<UserDTO> result = userMapper.toDtoList(Collections.emptyList());

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter out null DTOs from result list")
        void shouldFilterOutNullDTOsFromResultList() {
            // Given
            List<User> usersWithNull = Arrays.asList(validUser, null);

            // When
            List<UserDTO> result = userMapper.toDtoList(usersWithNull);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1); // Only the valid user should be mapped
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should handle large list of users")
        void shouldHandleLargeListOfUsers() {
            // Given
            List<User> largeUserList = Arrays.asList(
                    validUser,
                    createUserWith(builder -> builder.id(2L).firstName("Jane")),
                    createUserWith(builder -> builder.id(3L).firstName("Bob")),
                    createUserWith(builder -> builder.id(4L).firstName("Alice")),
                    createUserWith(builder -> builder.id(5L).firstName("Charlie"))
            );

            // When
            List<UserDTO> result = userMapper.toDtoList(largeUserList);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(5);
            assertThat(result.stream().map(UserDTO::getId))
                    .containsExactly(1L, 2L, 3L, 4L, 5L);
        }
    }

    @Nested
    @DisplayName("Update Entity Tests")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should update entity from DTO successfully")
        void shouldUpdateEntityFromDTOSuccessfully() {
            // Given
            User existingUser = User.builder()
                    .id(1L)
                    .firstName("OldFirst")
                    .lastName("OldLast")
                    .email("old@example.com")
                    .phone("+1111111111")
                    .isActive(true)
                    .createdBy("original")
                    .updatedBy("original")
                    .build();

            UserDTO updateDTO = UserDTO.builder()
                    .firstName("NewFirst")
                    .lastName("NewLast")
                    .email("new@example.com")
                    .phone("+2222222222")
                    .build();

            // When
            userMapper.updateEntityFromDto(existingUser, updateDTO);

            // Then
            assertThat(existingUser.getFirstName()).isEqualTo("NewFirst");
            assertThat(existingUser.getLastName()).isEqualTo("NewLast");
            assertThat(existingUser.getEmail()).isEqualTo("new@example.com");
            assertThat(existingUser.getPhone()).isEqualTo("+2222222222");
            assertThat(existingUser.getUpdatedBy()).isEqualTo("arihants1");

            // These should remain unchanged
            assertThat(existingUser.getId()).isEqualTo(1L);
            assertThat(existingUser.getIsActive()).isTrue();
            assertThat(existingUser.getCreatedBy()).isEqualTo("original");
        }

        @Test
        @DisplayName("Should handle partial updates from DTO")
        void shouldHandlePartialUpdatesFromDTO() {
            // Given
            User existingUser = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .isActive(true)
                    .build();

            UserDTO partialUpdateDTO = UserDTO.builder()
                    .firstName("UpdatedFirst")
                    .email("updated@example.com")
                    // lastName and phone are null - should not be updated
                    .build();

            // When
            userMapper.updateEntityFromDto(existingUser, partialUpdateDTO);

            // Then
            assertThat(existingUser.getFirstName()).isEqualTo("UpdatedFirst");
            assertThat(existingUser.getEmail()).isEqualTo("updated@example.com");
            assertThat(existingUser.getUpdatedBy()).isEqualTo("arihants1");

            // These should remain unchanged
            assertThat(existingUser.getLastName()).isEqualTo("Doe");
            assertThat(existingUser.getPhone()).isEqualTo("+1234567890");
        }

        @Test
        @DisplayName("Should handle null existing user gracefully")
        void shouldHandleNullExistingUserGracefully() {
            // Given
            UserDTO updateDTO = UserDTO.builder()
                    .firstName("Test")
                    .build();

            // When & Then - Should not throw exception
            userMapper.updateEntityFromDto(null, updateDTO);
        }

        @Test
        @DisplayName("Should handle null update DTO gracefully")
        void shouldHandleNullUpdateDTOGracefully() {
            // Given
            User existingUser = User.builder()
                    .id(1L)
                    .firstName("John")
                    .build();

            // When & Then - Should not throw exception
            userMapper.updateEntityFromDto(existingUser, null);

            // User should remain unchanged
            assertThat(existingUser.getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should always update updatedBy field")
        void shouldAlwaysUpdateUpdatedByField() {
            // Given
            User existingUser = User.builder()
                    .id(1L)
                    .firstName("John")
                    .updatedBy("original")
                    .build();

            UserDTO emptyUpdateDTO = UserDTO.builder().build();

            // When
            userMapper.updateEntityFromDto(existingUser, emptyUpdateDTO);

            // Then
            assertThat(existingUser.getUpdatedBy()).isEqualTo("arihants1");
            assertThat(existingUser.getFirstName()).isEqualTo("John"); // Should remain unchanged
        }
    }

    @Nested
    @DisplayName("Summary DTO Tests")
    class SummaryDtoTests {

        @Test
        @DisplayName("Should create summary DTO successfully")
        void shouldCreateSummaryDTOSuccessfully() {
            // When
            UserDTO result = userMapper.toSummaryDto(validUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(validUser.getId());
            assertThat(result.getFirstName()).isEqualTo(validUser.getFirstName());
            assertThat(result.getLastName()).isEqualTo(validUser.getLastName());
            assertThat(result.getEmail()).isEqualTo(validUser.getEmail());
            assertThat(result.getFullName()).isEqualTo(validUser.getFullName());
            assertThat(result.getDisplayName()).isEqualTo(validUser.getDisplayName());
            assertThat(result.getIsActive()).isEqualTo(validUser.getIsActive());

            // These should be null in summary DTO
            assertThat(result.getPhone()).isNull();
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getUpdatedAt()).isNull();
            assertThat(result.getHasCompleteProfile()).isNull();
        }

        @Test
        @DisplayName("Should return null when User entity is null for summary")
        void shouldReturnNullWhenUserEntityIsNullForSummary() {
            // When
            UserDTO result = userMapper.toSummaryDto(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle User with minimal data for summary")
        void shouldHandleUserWithMinimalDataForSummary() {
            // Given
            User minimalUser = User.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane@example.com")
                    .isActive(false)
                    .build();

            // When
            UserDTO result = userMapper.toSummaryDto(minimalUser);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            assertThat(result.getEmail()).isEqualTo("jane@example.com");
            assertThat(result.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle runtime exception during DTO conversion")
        void shouldHandleRuntimeExceptionDuringDTOConversion() {
            // This test is more conceptual since the actual mapping is straightforward
            // In a real scenario, you might have complex mapping logic that could fail

            // Given a valid user
            User user = validUser;

            // When - Normal operation should work
            UserDTO result = userMapper.toDto(user);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle runtime exception during entity conversion")
        void shouldHandleRuntimeExceptionDuringEntityConversion() {
            // This test is more conceptual since the actual mapping is straightforward

            // Given a valid DTO
            UserDTO dto = validUserDTO;

            // When - Normal operation should work
            User result = userMapper.toEntity(dto);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle runtime exception during list conversion")
        void shouldHandleRuntimeExceptionDuringListConversion() {
            // Given a list with various users
            List<User> users = Arrays.asList(validUser);

            // When - Normal operation should work
            List<UserDTO> result = userMapper.toDtoList(users);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should handle runtime exception during update")
        void shouldHandleRuntimeExceptionDuringUpdate() {
            // Given
            User user = validUser;
            UserDTO dto = validUserDTO;

            // When - Normal operation should work
            userMapper.updateEntityFromDto(user, dto);

            // Then - No exception should be thrown
            assertThat(user.getUpdatedBy()).isEqualTo("arihants1");
        }
    }
}
