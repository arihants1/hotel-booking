package com.hrs.hotelbooking.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrs.hotelbooking.hotel.service.HotelService;
import com.hrs.hotelbooking.shared.dto.HotelDTO;
import com.hrs.hotelbooking.shared.exception.ResourceNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HotelController.class)
public class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @Autowired
    private ObjectMapper objectMapper;

    // Test for /search endpoint
    @Test
    public void searchHotels_WithValidParams_ReturnsMatchingHotels() throws Exception {
        // Arrange
        List<HotelDTO> hotels = Arrays.asList(
            createHotelDTO(1L, "Grand Hotel", "New York", "USA", 5, 299.99),
            createHotelDTO(2L, "Plaza Hotel", "New York", "USA", 4, 199.99)
        );

        when(hotelService.searchHotels(eq("New York"), any(), any(), any(), anyInt(), anyInt()))
            .thenReturn(hotels);

        // Act & Assert
        mockMvc.perform(get("/api/v1/hotels/search")
                .param("city", "New York")
                .param("checkIn", "2025-07-15")
                .param("checkOut", "2025-07-20")
                .param("rooms", "1")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Grand Hotel"))
                .andExpect(jsonPath("$.data[1].name").value("Plaza Hotel"));

        // Verify service was called with correct parameters
        verify(hotelService).searchHotels(
            eq("New York"),
            eq(LocalDate.of(2025, 7, 15)),
            eq(LocalDate.of(2025, 7, 20)),
            eq(1),
            eq(0),
            eq(20)
        );
    }

    @Test
    public void searchHotels_WithNoParams_ReturnsAllHotels() throws Exception {
        // Arrange
        List<HotelDTO> hotels = Arrays.asList(
            createHotelDTO(1L, "Grand Hotel", "New York", "USA", 5, 299.99),
            createHotelDTO(2L, "Plaza Hotel", "New York", "USA", 4, 199.99)
        );

        when(hotelService.searchHotels(isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
            .thenReturn(hotels);

        // Act & Assert
        mockMvc.perform(get("/api/v1/hotels/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // Test for /{id} endpoint
    @Test
    public void getHotelById_WithValidId_ReturnsHotel() throws Exception {
        // Arrange
        HotelDTO hotel = createHotelDTO(1L, "Grand Hotel", "New York", "USA", 5, 299.99);
        when(hotelService.getHotelById(1L)).thenReturn(hotel);

        // Act & Assert
        mockMvc.perform(get("/api/v1/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Grand Hotel"));
    }

    @Test
    public void getHotelById_WithInvalidId_Returns404() throws Exception {
        // Arrange
        when(hotelService.getHotelById(99L)).thenThrow(new ResourceNotFoundException("Hotel", "id", 99L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/hotels/99"))
                .andExpect(status().isNotFound());
    }

    // Test for GET all hotels endpoint
    @Test
    public void getAllHotels_ReturnsPagedHotels() throws Exception {
        // Arrange
        List<HotelDTO> hotels = Arrays.asList(
            createHotelDTO(1L, "Grand Hotel", "New York", "USA", 5, 299.99),
            createHotelDTO(2L, "Plaza Hotel", "New York", "USA", 4, 199.99)
        );
        Page<HotelDTO> hotelPage = new PageImpl<>(hotels);
        when(hotelService.getAllHotels(anyInt(), anyInt())).thenReturn(hotelPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/hotels")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    // Test for /city/{city} endpoint
    @Test
    public void getHotelsByCity_WithValidCity_ReturnsHotels() throws Exception {
        // Arrange
        List<HotelDTO> hotels = Arrays.asList(
            createHotelDTO(1L, "Grand Hotel", "New York", "USA", 5, 299.99),
            createHotelDTO(2L, "Plaza Hotel", "New York", "USA", 4, 199.99)
        );
        when(hotelService.getHotelsByCity("New York")).thenReturn(hotels);

        // Act & Assert
        mockMvc.perform(get("/api/v1/hotels/city/New York"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].city").value("New York"));
    }

    // Test for POST create hotel endpoint
    @Test
    public void createHotel_WithValidData_CreatesHotel() throws Exception {
        // Arrange
        HotelDTO inputHotel = createHotelDTO(null, "New Hotel", "Paris", "France", 5, 399.99);
        HotelDTO createdHotel = createHotelDTO(3L, "New Hotel", "Paris", "France", 5, 399.99);
        when(hotelService.createHotel(any(HotelDTO.class))).thenReturn(createdHotel);

        // Act & Assert
        mockMvc.perform(post("/api/v1/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputHotel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(3));

        // Verify correct content was passed to service
        ArgumentCaptor<HotelDTO> hotelCaptor = ArgumentCaptor.forClass(HotelDTO.class);
        verify(hotelService).createHotel(hotelCaptor.capture());
        assertEquals("New Hotel", hotelCaptor.getValue().getName());
        assertEquals("Paris", hotelCaptor.getValue().getCity());
    }

    @Test
    public void createHotel_WithInvalidData_ReturnsBadRequest() throws Exception {
        // Arrange
        HotelDTO invalidHotel = new HotelDTO(); // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/v1/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidHotel)))
                .andExpect(status().isBadRequest());
    }

    // Test for PUT update hotel endpoint
    @Test
    public void updateHotel_WithValidData_UpdatesHotel() throws Exception {
        // Arrange
        HotelDTO inputHotel = createHotelDTO(1L, "Updated Hotel", "New York", "USA", 4, 279.99);
        HotelDTO updatedHotel = createHotelDTO(1L, "Updated Hotel", "New York", "USA", 4, 279.99);
        when(hotelService.updateHotel(eq(1L), any(HotelDTO.class))).thenReturn(updatedHotel);

        // Act & Assert
        mockMvc.perform(put("/api/v1/hotels/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputHotel)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Hotel"));
    }

    @Test
    public void updateHotel_WithInvalidId_Returns404() throws Exception {
        // Arrange
        HotelDTO inputHotel = createHotelDTO(99L, "NonExistent Hotel", "London", "UK", 3, 159.99);
        when(hotelService.updateHotel(eq(99L), any(HotelDTO.class)))
            .thenThrow(new ResourceNotFoundException("Hotel", "id", 99L));

        // Act & Assert
        mockMvc.perform(put("/api/v1/hotels/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputHotel)))
                .andExpect(status().isNotFound());
    }

    // Test for DELETE endpoint
    @Test
    public void deleteHotel_WithValidId_DeletesHotel() throws Exception {
        // Arrange
        doNothing().when(hotelService).deleteHotel(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify service was called
        verify(hotelService).deleteHotel(1L);
    }

    @Test
    public void deleteHotel_WithInvalidId_Returns404() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Hotel", "id", 99L))
            .when(hotelService).deleteHotel(99L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/hotels/99"))
                .andExpect(status().isNotFound());
    }

    // Helper method to create hotel DTOs for testing
    private HotelDTO createHotelDTO(Long id, String name, String city, String country, Integer starRating, Double basePrice) {
        HotelDTO hotel = new HotelDTO();
        hotel.setId(id);
        hotel.setName(name);
        hotel.setCity(city);
        hotel.setCountry(country);
        hotel.setStarRating(starRating);
        hotel.setBasePrice(BigDecimal.valueOf(basePrice));
        hotel.setLocation(city + ", " + country);
        return hotel;
    }
}
