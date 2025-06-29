package com.hrs.hotelbooking.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrs.hotelbooking.shared.dto.UserDTO;
import com.hrs.hotelbooking.shared.exception.ResourceNotFoundException;
import com.hrs.hotelbooking.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createUser_WithValidData_ReturnsCreatedUser() throws Exception {
        // Arrange
        UserDTO inputUser = createUserDTO(null, "John", "Doe", "john.doe@example.com");
        UserDTO createdUser = createUserDTO(1L, "John", "Doe", "john.doe@example.com");

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

        // Verify service was called with correct data
        ArgumentCaptor<UserDTO> userCaptor = ArgumentCaptor.forClass(UserDTO.class);
        verify(userService).createUser(userCaptor.capture());
        assertEquals("John", userCaptor.getValue().getFirstName());
        assertEquals("Doe", userCaptor.getValue().getLastName());
    }

    @Test
    public void createUser_WithInvalidData_ReturnsBadRequest() throws Exception {
        // Arrange - Use an invalid user without required fields
        UserDTO invalidUser = new UserDTO();
        invalidUser.setFirstName("John"); // Missing last name and email

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getUserById_WithValidId_ReturnsUser() throws Exception {
        // Arrange
        UserDTO user = createUserDTO(1L, "John", "Doe", "john.doe@example.com");
        when(userService.getUserById(1L)).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }

    @Test
    public void getUserById_WithInvalidId_Returns404() throws Exception {
        // Arrange
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User", "id", 99L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUserByEmail_WithValidEmail_ReturnsUser() throws Exception {
        // Arrange
        UserDTO user = createUserDTO(1L, "John", "Doe", "john.doe@example.com");
        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    public void getUserByEmail_WithInvalidEmail_Returns404() throws Exception {
        // Arrange
        when(userService.getUserByEmail("nonexistent@example.com"))
                .thenThrow(new ResourceNotFoundException("User", "email", "nonexistent@example.com"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/email/nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUser_WithValidData_ReturnsUpdatedUser() throws Exception {
        // Arrange
        UserDTO inputUser = createUserDTO(1L, "Johnny", "Doe", "john.doe@example.com");
        UserDTO updatedUser = createUserDTO(1L, "Johnny", "Doe", "john.doe@example.com");

        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Johnny"));
    }

    @Test
    public void updateUser_WithInvalidId_Returns404() throws Exception {
        // Arrange
        UserDTO inputUser = createUserDTO(99L, "Johnny", "Doe", "john.doe@example.com");
        when(userService.updateUser(eq(99L), any(UserDTO.class)))
                .thenThrow(new ResourceNotFoundException("User", "id", 99L));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteUser_WithValidId_ReturnsSuccess() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify service was called
        verify(userService).deleteUser(1L);
    }

    @Test
    public void deleteUser_WithInvalidId_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("User", "id", 99L))
                .when(userService).deleteUser(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void searchUsersByName_ReturnsMatchingUsers() throws Exception {
        // Arrange
        List<UserDTO> users = Arrays.asList(
                createUserDTO(1L, "John", "Doe", "john.doe@example.com")
        );

        when(userService.searchUsersByName("John")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search/name")
                .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].firstName").value("John"));
    }

    @Test
    public void searchUsersByEmail_ReturnsMatchingUsers() throws Exception {
        // Arrange
        List<UserDTO> users = Arrays.asList(
                createUserDTO(1L, "John", "Doe", "john.doe@example.com")
        );

        when(userService.searchUsersByEmail("john")).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/search/email")
                .param("email", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value("john.doe@example.com"));
    }

    @Test
    public void getUserCount_ReturnsCount() throws Exception {
        // Arrange
        when(userService.getUserCount()).thenReturn(10L);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));
    }

    // Helper method to create UserDTO test objects
    private UserDTO createUserDTO(Long id, String firstName, String lastName, String email) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone("+12025550199");
        user.setIsActive(true);
        return user;
    }
}
