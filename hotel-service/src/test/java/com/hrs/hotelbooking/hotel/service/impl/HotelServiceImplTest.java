package com.hrs.hotelbooking.hotel.service.impl;

import com.hrs.hotelbooking.hotel.entity.HotelSearchDocument;
import com.hrs.hotelbooking.hotel.mapper.HotelMapper;
import com.hrs.hotelbooking.hotel.repository.HotelRepository;
import com.hrs.hotelbooking.hotel.repository.HotelSearchRepository;
import com.hrs.hotelbooking.shared.dto.HotelDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.hrs.hotelbooking.hotel.service.impl.HotelServiceImpl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @Mock
    private HotelSearchRepository hotelSearchRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private HotelSearchDocument hotelSearchDoc1;
    private HotelSearchDocument hotelSearchDoc2;

    @BeforeEach
    void setUp() {
        // Setup test data
        hotelSearchDoc1 = new HotelSearchDocument(
                1L,
                "HRS Grand Hotel",
                "New York",
                "USA",
                5,
                new BigDecimal("199.99"),
                true
        );

        hotelSearchDoc2 = new HotelSearchDocument(
                2L,
                "HRS Business Hotel",
                "New York",
                "USA",
                4,
                new BigDecimal("149.99"),
                true
        );
    }

    @Test
    @DisplayName("Search hotels by city should return matching hotels")
    void searchHotelsByCity_ShouldReturnMatchingHotels() {
        // Arrange
        String city = "New York";
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        Integer rooms = 1;
        int page = 0;
        int size = 10;

        List<HotelSearchDocument> mockResults = Arrays.asList(hotelSearchDoc1, hotelSearchDoc2);

        when(hotelSearchRepository.findByCityAndIsActive(city, true)).thenReturn(mockResults);

        // Act
        List<HotelDTO> result = hotelService.searchHotels(city, checkIn, checkOut, rooms, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(hotelSearchDoc1.getId());
        assertThat(result.get(0).getName()).isEqualTo(hotelSearchDoc1.getName());
        assertThat(result.get(0).getCity()).isEqualTo(hotelSearchDoc1.getCity());
        assertThat(result.get(0).getCountry()).isEqualTo(hotelSearchDoc1.getCountry());
        assertThat(result.get(0).getStarRating()).isEqualTo(hotelSearchDoc1.getStarRating());
        assertThat(result.get(0).getBasePrice()).isEqualTo(hotelSearchDoc1.getBasePrice());

        assertThat(result.get(1).getId()).isEqualTo(hotelSearchDoc2.getId());
        assertThat(result.get(1).getName()).isEqualTo(hotelSearchDoc2.getName());

        // Verify interactions
        verify(hotelSearchRepository).findByCityAndIsActive(city, true);
        verifyNoMoreInteractions(hotelSearchRepository);
    }

    @Test
    @DisplayName("Search hotels without city should return all hotels")
    void searchHotelsWithoutCity_ShouldReturnAllHotels() {
        // Arrange
        String city = null;
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        Integer rooms = 1;
        int page = 0;
        int size = 10;

        List<HotelSearchDocument> mockResults = Arrays.asList(hotelSearchDoc1, hotelSearchDoc2);

        when(hotelSearchRepository.findAll()).thenReturn(mockResults);

        // Act
        List<HotelDTO> result = hotelService.searchHotels(city, checkIn, checkOut, rooms, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(hotelSearchDoc1.getId());
        assertThat(result.get(1).getId()).isEqualTo(hotelSearchDoc2.getId());

        // Verify interactions
        verify(hotelSearchRepository).findAll();
        verifyNoMoreInteractions(hotelSearchRepository);
    }

    @Test
    @DisplayName("Search hotels with empty city string should return all hotels")
    void searchHotelsWithEmptyCity_ShouldReturnAllHotels() {
        // Arrange
        String city = "";
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        Integer rooms = 1;
        int page = 0;
        int size = 10;

        List<HotelSearchDocument> mockResults = Arrays.asList(hotelSearchDoc1, hotelSearchDoc2);

        when(hotelSearchRepository.findAll()).thenReturn(mockResults);

        // Act
        List<HotelDTO> result = hotelService.searchHotels(city, checkIn, checkOut, rooms, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // Verify interactions
        verify(hotelSearchRepository).findAll();
        verifyNoMoreInteractions(hotelSearchRepository);
    }

    @Test
    @DisplayName("Search hotels with city should return empty list when no matches")
    void searchHotelsByCity_ShouldReturnEmptyList_WhenNoMatches() {
        // Arrange
        String city = "Chicago";
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        Integer rooms = 1;
        int page = 0;
        int size = 10;

        when(hotelSearchRepository.findByCityAndIsActive(city, true)).thenReturn(Collections.emptyList());

        // Act
        List<HotelDTO> result = hotelService.searchHotels(city, checkIn, checkOut, rooms, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify interactions
        verify(hotelSearchRepository).findByCityAndIsActive(city, true);
        verifyNoMoreInteractions(hotelSearchRepository);
    }
}
