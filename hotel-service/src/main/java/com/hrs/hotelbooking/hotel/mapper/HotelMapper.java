package com.hrs.hotelbooking.hotel.mapper;

import com.hrs.hotelbooking.hotel.entity.Hotel;
import com.hrs.hotelbooking.shared.dto.HotelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HRS Hotel Mapper - Essential Mappings Only
 * Converts between Hotel entity and HotelDTO
 *
 * @author arihants1
 */
@Component
@Slf4j
public class HotelMapper {

    /**
     * Convert Hotel entity to HotelDTO
     */
    public HotelDTO toDto(Hotel hotel) {
        if (hotel == null) {
            log.warn("Attempting to convert null Hotel entity to DTO");
            return null;
        }

        try {
            return HotelDTO.builder()
                    .id(hotel.getId())
                    .name(hotel.getName())
                    .description(hotel.getDescription())
                    .location(hotel.getLocation())
                    .city(hotel.getCity())
                    .country(hotel.getCountry())
                    .starRating(hotel.getStarRating())
                    .amenities(hotel.getAmenities())
                    .basePrice(hotel.getBasePrice())
                    .totalRooms(hotel.getTotalRooms())
                    .phone(hotel.getPhone())
                    .email(hotel.getEmail())
                    .website(hotel.getWebsite())
                    .createdAt(hotel.getCreatedAt())
                    .updatedAt(hotel.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting Hotel entity {} to DTO ",
                    hotel.getId(), e.getMessage());
            throw new RuntimeException("Failed to convert Hotel to DTO", e);
        }
    }

    /**
     * Convert HotelDTO to Hotel entity
     */
    public Hotel toEntity(HotelDTO hotelDTO) {
        if (hotelDTO == null) {
            log.warn("Attempting to convert null HotelDTO to entity ");
            return null;
        }

        try {
            return Hotel.builder()
                    .id(hotelDTO.getId())
                    .name(hotelDTO.getName())
                    .description(hotelDTO.getDescription())
                    .location(hotelDTO.getLocation())
                    .city(hotelDTO.getCity())
                    .country(hotelDTO.getCountry())
                    .starRating(hotelDTO.getStarRating())
                    .amenities(hotelDTO.getAmenities())
                    .basePrice(hotelDTO.getBasePrice())
                    .totalRooms(hotelDTO.getTotalRooms())
                    .phone(hotelDTO.getPhone())
                    .email(hotelDTO.getEmail())
                    .website(hotelDTO.getWebsite())
                    .createdAt(hotelDTO.getCreatedAt())
                    .updatedAt(hotelDTO.getUpdatedAt())
                    .isActive(true)
                    .createdBy("arihants1")
                    .updatedBy("arihants1")
                    .build();
        } catch (Exception e) {
            log.error("Error converting HotelDTO {} to entity {}",
                    hotelDTO.getName(), e.getMessage());
            throw new RuntimeException("Failed to convert HotelDTO to entity", e);
        }
    }

    /**
     * Convert list of Hotel entities to HotelDTO list
     */
    public List<HotelDTO> toDtoList(List<Hotel> hotels) {
        if (hotels == null || hotels.isEmpty()) {
            log.debug("Converting empty or null hotel list to DTOs");
            return List.of();
        }

        try {
            List<HotelDTO> result = hotels.stream()
                    .map(this::toDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            log.debug("Successfully converted {} hotels to DTOs", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error converting hotel list to DTOs {}", e.getMessage());
            throw new RuntimeException("Failed to convert hotel list to DTOs", e);
        }
    }

    /**
     * Update existing Hotel entity with HotelDTO data
     */
    public void updateEntityFromDto(Hotel existingHotel, HotelDTO hotelDTO) {
        if (existingHotel == null || hotelDTO == null) {
            log.warn("Cannot update hotel entity: hotel or DTO is null ");
            return;
        }

        try {
            if (hotelDTO.getName() != null) {
                existingHotel.setName(hotelDTO.getName());
            }
            if (hotelDTO.getDescription() != null) {
                existingHotel.setDescription(hotelDTO.getDescription());
            }
            if (hotelDTO.getLocation() != null) {
                existingHotel.setLocation(hotelDTO.getLocation());
            }
            if (hotelDTO.getCity() != null) {
                existingHotel.setCity(hotelDTO.getCity());
            }
            if (hotelDTO.getCountry() != null) {
                existingHotel.setCountry(hotelDTO.getCountry());
            }
            if (hotelDTO.getStarRating() != null) {
                existingHotel.setStarRating(hotelDTO.getStarRating());
            }
            if (hotelDTO.getAmenities() != null) {
                existingHotel.setAmenities(hotelDTO.getAmenities());
            }
            if (hotelDTO.getBasePrice() != null) {
                existingHotel.setBasePrice(hotelDTO.getBasePrice());
            }
            if (hotelDTO.getTotalRooms() != null) {
                existingHotel.setTotalRooms(hotelDTO.getTotalRooms());
            }
            if (hotelDTO.getPhone() != null) {
                existingHotel.setPhone(hotelDTO.getPhone());
            }
            if (hotelDTO.getEmail() != null) {
                existingHotel.setEmail(hotelDTO.getEmail());
            }
            if (hotelDTO.getWebsite() != null) {
                existingHotel.setWebsite(hotelDTO.getWebsite());
            }

            // Always update the updatedBy field
            existingHotel.setUpdatedBy("arihants1");

            log.debug("Successfully updated hotel entity {} from DTO", existingHotel.getId());
        } catch (Exception e) {
            log.error("Error updating hotel entity {} from DTO {}",
                    existingHotel.getId(), e.getMessage());
            throw new RuntimeException("Failed to update hotel entity from DTO", e);
        }
    }
}