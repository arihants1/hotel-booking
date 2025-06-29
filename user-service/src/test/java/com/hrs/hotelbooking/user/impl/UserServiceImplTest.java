package com.hrs.hotelbooking.user.impl;

import com.hrs.hotelbooking.shared.dto.UserDTO;
import com.hrs.hotelbooking.shared.exception.BusinessValidationException;
import com.hrs.hotelbooking.shared.exception.ResourceNotFoundException;
import com.hrs.hotelbooking.user.entity.User;
import com.hrs.hotelbooking.user.mapper.UserMapper;
import com.hrs.hotelbooking.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Test cases for UserServiceImpl
 * Tests all user service operations including validation, CRUD operations, and search functionality
 *
 * @author arihants1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Implementation Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserDTO validUserDTO;
    private User validUser;
    private UserDTO updatedUserDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

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
                .createdAt(now)
                .updatedAt(now)
                .build();

        validUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("arihants1")
                .updatedBy("arihants1")
                .build();

        updatedUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("+9876543210")
                .isActive(true)
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
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void shouldCreateUserSuccessfullyWithValidData() {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .phone("+1234567890")
                    .build();

            given(userRepository.existsByEmailIgnoreCase(inputDTO.getEmail())).willReturn(false);
            given(userMapper.toEntity(inputDTO)).willReturn(validUser);
            given(userRepository.save(any(User.class))).willReturn(validUser);
            given(userMapper.toDto(validUser)).willReturn(validUserDTO);

            // When
            UserDTO result = userService.createUser(inputDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");

            verify(userRepository).existsByEmailIgnoreCase(inputDTO.getEmail());
            verify(userMapper).toEntity(inputDTO);
            verify(userRepository).save(any(User.class));
            verify(userMapper).toDto(validUser);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .build();

            given(userRepository.existsByEmailIgnoreCase(inputDTO.getEmail())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(inputDTO))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Email already exists: john.doe@example.com");

            verify(userRepository).existsByEmailIgnoreCase(inputDTO.getEmail());
            verify(userMapper, never()).toEntity(any());
            verify(userRepository, never()).save(any());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("Should throw exception for invalid first name")
        void shouldThrowExceptionForInvalidFirstName(String firstName) {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .firstName(firstName)
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.createUser(inputDTO))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("First name is required");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("Should throw exception for invalid last name")
        void shouldThrowExceptionForInvalidLastName(String lastName) {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName(lastName)
                    .email("john.doe@example.com")
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.createUser(inputDTO))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Last name is required");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("Should throw exception for invalid email")
        void shouldThrowExceptionForInvalidEmail(String email) {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email(email)
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.createUser(inputDTO))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Email is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "test@", "@example.com", "test.example.com", "test@.com"})
        @DisplayName("Should throw exception for malformed email")
        void shouldThrowExceptionForMalformedEmail(String email) {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email(email)
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.createUser(inputDTO))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Invalid email format");
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "abc", "123-456", "12345678901234567"})
        @DisplayName("Should throw exception for invalid phone format")
        void shouldThrowExceptionForInvalidPhoneFormat(String phone) {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .phone(phone)
                    .build();

            // When & Then
            assertThatThrownBy(() -> userService.createUser(inputDTO))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Invalid phone format");
        }

        @Test
        @DisplayName("Should throw exception for null user DTO")
        void shouldThrowExceptionForNullUserDTO() {
            // When & Then
            assertThatThrownBy(() -> userService.createUser(null))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("User information is required");
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            // Given
            Long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.of(validUser));
            given(userMapper.toDto(validUser)).willReturn(validUserDTO);

            // When
            UserDTO result = userService.getUserById(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

            verify(userRepository).findById(userId);
            verify(userMapper).toDto(validUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void shouldThrowExceptionWhenUserNotFoundById() {
            // Given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with id: 999");

            verify(userRepository).findById(userId);
            verify(userMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("Should throw exception when user is inactive")
        void shouldThrowExceptionWhenUserIsInactive() {
            // Given
            Long userId = 1L;
            User inactiveUser = createUserWith(builder -> builder.isActive(false));
            given(userRepository.findById(userId)).willReturn(Optional.of(inactiveUser));

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with id: 1");

            verify(userRepository).findById(userId);
            verify(userMapper, never()).toDto(any());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -999L})
        @DisplayName("Should throw exception for invalid user ID")
        void shouldThrowExceptionForInvalidUserId(Long userId) {
            // When & Then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Invalid user ID provided");
        }

        @Test
        @DisplayName("Should throw exception for null user ID")
        void shouldThrowExceptionForNullUserId() {
            // When & Then
            assertThatThrownBy(() -> userService.getUserById(null))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Invalid user ID provided");
        }

        @Test
        @DisplayName("Should get user by email successfully")
        void shouldGetUserByEmailSuccessfully() {
            // Given
            String email = "john.doe@example.com";
            given(userRepository.findByEmailIgnoreCase(email)).willReturn(Optional.of(validUser));
            given(userMapper.toDto(validUser)).willReturn(validUserDTO);

            // When
            UserDTO result = userService.getUserByEmail(email);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);

            verify(userRepository).findByEmailIgnoreCase(email);
            verify(userMapper).toDto(validUser);
        }

        @Test
        @DisplayName("Should throw exception when user not found by email")
        void shouldThrowExceptionWhenUserNotFoundByEmail() {
            // Given
            String email = "notfound@example.com";
            given(userRepository.findByEmailIgnoreCase(email)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserByEmail(email))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with email: notfound@example.com");

            verify(userRepository).findByEmailIgnoreCase(email);
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given
            Long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.of(validUser));
            given(userRepository.save(any(User.class))).willReturn(validUser);
            given(userMapper.toDto(validUser)).willReturn(updatedUserDTO);

            // When
            UserDTO result = userService.updateUser(userId, updatedUserDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);

            verify(userRepository).findById(userId);
            verify(userMapper).updateEntityFromDto(validUser, updatedUserDTO);
            verify(userRepository).save(validUser);
            verify(userMapper).toDto(validUser);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent user")
        void shouldThrowExceptionWhenUpdatingNonExistentUser() {
            // Given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(userId, updatedUserDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with id: 999");

            verify(userRepository).findById(userId);
            verify(userMapper, never()).updateEntityFromDto(any(), any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when updating inactive user")
        void shouldThrowExceptionWhenUpdatingInactiveUser() {
            // Given
            Long userId = 1L;
            User inactiveUser = createUserWith(builder -> builder.isActive(false));
            given(userRepository.findById(userId)).willReturn(Optional.of(inactiveUser));

            // When & Then
            assertThatThrownBy(() -> userService.updateUser(userId, updatedUserDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with id: 1");

            verify(userRepository).findById(userId);
            verify(userMapper, never()).updateEntityFromDto(any(), any());
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should soft delete user successfully")
        void shouldSoftDeleteUserSuccessfully() {
            // Given
            Long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.of(validUser));
            given(userRepository.save(any(User.class))).willReturn(validUser);

            // When
            userService.deleteUser(userId);

            // Then
            verify(userRepository).findById(userId);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getIsActive()).isFalse();
            assertThat(savedUser.getUpdatedBy()).isEqualTo("arihants1");
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent user")
        void shouldThrowExceptionWhenDeletingNonExistentUser() {
            // Given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with id: 999");

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when deleting already inactive user")
        void shouldThrowExceptionWhenDeletingAlreadyInactiveUser() {
            // Given
            Long userId = 1L;
            User inactiveUser = createUserWith(builder -> builder.isActive(false));
            given(userRepository.findById(userId)).willReturn(Optional.of(inactiveUser));

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("User not found with id: 1");

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Search Users Tests")
    class SearchUsersTests {

        @Test
        @DisplayName("Should search users by name successfully")
        void shouldSearchUsersByNameSuccessfully() {
            // Given
            String searchName = "John";
            List<User> users = Arrays.asList(validUser);
            List<UserDTO> userDTOs = Arrays.asList(validUserDTO);

            given(userRepository.findByNameContainingIgnoreCase(searchName)).willReturn(users);
            given(userMapper.toDtoList(users)).willReturn(userDTOs);

            // When
            List<UserDTO> result = userService.searchUsersByName(searchName);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).isEqualTo("John");

            verify(userRepository).findByNameContainingIgnoreCase(searchName);
            verify(userMapper).toDtoList(users);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("Should throw exception for empty search name")
        void shouldThrowExceptionForEmptySearchName(String searchName) {
            // When & Then
            assertThatThrownBy(() -> userService.searchUsersByName(searchName))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Search name cannot be empty");
        }

        @Test
        @DisplayName("Should search users by email successfully")
        void shouldSearchUsersByEmailSuccessfully() {
            // Given
            String emailPattern = "john";
            List<User> users = Arrays.asList(validUser);
            List<UserDTO> userDTOs = Arrays.asList(validUserDTO);

            given(userRepository.findByEmailContainingIgnoreCase(emailPattern)).willReturn(users);
            given(userMapper.toDtoList(users)).willReturn(userDTOs);

            // When
            List<UserDTO> result = userService.searchUsersByEmail(emailPattern);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).contains("john");

            verify(userRepository).findByEmailContainingIgnoreCase(emailPattern);
            verify(userMapper).toDtoList(users);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("Should throw exception for empty search email")
        void shouldThrowExceptionForEmptySearchEmail(String searchEmail) {
            // When & Then
            assertThatThrownBy(() -> userService.searchUsersByEmail(searchEmail))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessage("Search email cannot be empty");
        }

        @Test
        @DisplayName("Should return empty list when no users found by name")
        void shouldReturnEmptyListWhenNoUsersFoundByName() {
            // Given
            String searchName = "NonExistent";
            given(userRepository.findByNameContainingIgnoreCase(searchName)).willReturn(Collections.emptyList());
            given(userMapper.toDtoList(Collections.emptyList())).willReturn(Collections.emptyList());

            // When
            List<UserDTO> result = userService.searchUsersByName(searchName);

            // Then
            assertThat(result).isEmpty();

            verify(userRepository).findByNameContainingIgnoreCase(searchName);
            verify(userMapper).toDtoList(Collections.emptyList());
        }
    }

    @Nested
    @DisplayName("User Count Tests")
    class UserCountTests {

        @Test
        @DisplayName("Should get user count successfully")
        void shouldGetUserCountSuccessfully() {
            // Given
            Long expectedCount = 42L;
            given(userRepository.countByIsActiveTrue()).willReturn(expectedCount);

            // When
            Long result = userService.getUserCount();

            // Then
            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).countByIsActiveTrue();
        }

        @Test
        @DisplayName("Should return zero when no active users")
        void shouldReturnZeroWhenNoActiveUsers() {
            // Given
            given(userRepository.countByIsActiveTrue()).willReturn(0L);

            // When
            Long result = userService.getUserCount();

            // Then
            assertThat(result).isZero();
            verify(userRepository).countByIsActiveTrue();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid email formats")
        void shouldAcceptValidEmailFormats() {
            // Given
            List<String> validEmails = Arrays.asList(
                    "test@example.com",
                    "user.name@domain.co.uk",
                    "user+tag@example.org",
                    "user123@test-domain.com"
            );

            for (String email : validEmails) {
                UserDTO userDTO = UserDTO.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email(email)
                        .build();

                given(userRepository.existsByEmailIgnoreCase(email)).willReturn(false);
                given(userMapper.toEntity(userDTO)).willReturn(validUser);
                given(userRepository.save(any(User.class))).willReturn(validUser);
                given(userMapper.toDto(validUser)).willReturn(validUserDTO);

                // When & Then
                assertThat(userService.createUser(userDTO)).isNotNull();
            }
        }

        @Test
        @DisplayName("Should accept valid phone formats")
        void shouldAcceptValidPhoneFormats() {
            // Given
            List<String> validPhones = Arrays.asList(
                    "+1234567890",
                    "+44 20 7946 0958",
                    "+1-555-123-4567",
                    "+91 98765 43210"
            );

            for (String phone : validPhones) {
                UserDTO userDTO = UserDTO.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("test@example.com")
                        .phone(phone)
                        .build();

                given(userRepository.existsByEmailIgnoreCase(anyString())).willReturn(false);
                given(userMapper.toEntity(userDTO)).willReturn(validUser);
                given(userRepository.save(any(User.class))).willReturn(validUser);
                given(userMapper.toDto(validUser)).willReturn(validUserDTO);

                // When & Then
                assertThat(userService.createUser(userDTO)).isNotNull();
            }
        }
    }
}
