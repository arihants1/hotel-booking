package com.hrs.hotelbooking.hotel.service;

import com.hrs.hotelbooking.shared.dto.HotelDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * HRS Hotel Service Interface - Essential Features Only
 * Core hotel operations for the HRS booking system
 *
 * @author arihants1
 */
public interface HotelService {

    /**
     * Search hotels by city and dates
     */
    List<HotelDTO> searchHotels(String city, LocalDate checkIn, LocalDate checkOut,
                                Integer rooms, int page, int size);

    /**
     * Get hotel by ID
     */
    HotelDTO getHotelById(Long id);

    /**
     * Get all hotels with pagination
     */
    Page<HotelDTO> getAllHotels(int page, int size);

    /**
     * Get hotels by city
     */
    List<HotelDTO> getHotelsByCity(String city);

    /**
     * Create new hotel
     */
    HotelDTO createHotel(HotelDTO hotelDTO);

    /**
     * Update hotel
     */
    HotelDTO updateHotel(Long id, HotelDTO hotelDTO);

    /**
     * Delete hotel
     */
    void deleteHotel(Long id);
}