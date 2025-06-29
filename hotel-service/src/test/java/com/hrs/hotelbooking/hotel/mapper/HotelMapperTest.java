package com.hrs.hotelbooking.hotel.mapper;

import com.hrs.hotelbooking.hotel.entity.Hotel;
import com.hrs.hotelbooking.shared.dto.HotelDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HotelMapperTest {
    private HotelMapper hotelMapper;

    @BeforeEach
    void setUp() {
        hotelMapper = new HotelMapper();
    }

    @Test
    void toDto_shouldMapAllFields() {
        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .description("Desc")
                .location("Loc")
                .city("City")
                .country("Country")
                .starRating(5)
                .amenities(Map.of("Pool", "WiFi" ))
                .basePrice(new BigDecimal("123.45"))
                .totalRooms(100)
                .phone("1234567890")
                .email("test@hotel.com")
                .website("hotel.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        HotelDTO dto = hotelMapper.toDto(hotel);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(hotel.getId());
        assertThat(dto.getName()).isEqualTo(hotel.getName());
        assertThat(dto.getDescription()).isEqualTo(hotel.getDescription());
        assertThat(dto.getLocation()).isEqualTo(hotel.getLocation());
        assertThat(dto.getCity()).isEqualTo(hotel.getCity());
        assertThat(dto.getCountry()).isEqualTo(hotel.getCountry());
        assertThat(dto.getStarRating()).isEqualTo(hotel.getStarRating());
        assertThat(dto.getAmenities()).isEqualTo(hotel.getAmenities());
        assertThat(dto.getBasePrice()).isEqualTo(hotel.getBasePrice());
        assertThat(dto.getTotalRooms()).isEqualTo(hotel.getTotalRooms());
        assertThat(dto.getPhone()).isEqualTo(hotel.getPhone());
        assertThat(dto.getEmail()).isEqualTo(hotel.getEmail());
        assertThat(dto.getWebsite()).isEqualTo(hotel.getWebsite());
    }

    @Test
    void toDto_shouldReturnNullForNullInput() {
        assertThat(hotelMapper.toDto(null)).isNull();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        HotelDTO dto = HotelDTO.builder()
                .id(2L)
                .name("HotelDTO")
                .description("DTO Desc")
                .location("DTO Loc")
                .city("DTO City")
                .country("DTO Country")
                .starRating(4)
                .amenities(Map.of("Gym", "Spa" ))
                .basePrice(new BigDecimal("200.00"))
                .totalRooms(50)
                .phone("9876543210")
                .email("dto@hotel.com")
                .website("dtohotel.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Hotel entity = hotelMapper.toEntity(dto);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getLocation()).isEqualTo(dto.getLocation());
        assertThat(entity.getCity()).isEqualTo(dto.getCity());
        assertThat(entity.getCountry()).isEqualTo(dto.getCountry());
        assertThat(entity.getStarRating()).isEqualTo(dto.getStarRating());
        assertThat(entity.getAmenities()).isEqualTo(dto.getAmenities());
        assertThat(entity.getBasePrice()).isEqualTo(dto.getBasePrice());
        assertThat(entity.getTotalRooms()).isEqualTo(dto.getTotalRooms());
        assertThat(entity.getPhone()).isEqualTo(dto.getPhone());
        assertThat(entity.getEmail()).isEqualTo(dto.getEmail());
        assertThat(entity.getWebsite()).isEqualTo(dto.getWebsite());
    }

    @Test
    void toEntity_shouldReturnNullForNullInput() {
        assertThat(hotelMapper.toEntity(null)).isNull();
    }

    @Test
    void toDtoList_shouldMapList() {
        Hotel h1 = Hotel.builder().id(1L).name("H1").build();
        Hotel h2 = Hotel.builder().id(2L).name("H2").build();
        List<HotelDTO> dtos = hotelMapper.toDtoList(Arrays.asList(h1, h2));
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(1L);
        assertThat(dtos.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void toDtoList_shouldReturnEmptyListForNullOrEmpty() {
        assertThat(hotelMapper.toDtoList(null)).isEmpty();
        assertThat(hotelMapper.toDtoList(Collections.emptyList())).isEmpty();
    }
}

