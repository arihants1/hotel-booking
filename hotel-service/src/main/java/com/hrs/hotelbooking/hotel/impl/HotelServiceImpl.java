package com.hrs.hotelbooking.hotel.service.impl;

import com.hrs.hotelbooking.hotel.entity.Hotel;
import com.hrs.hotelbooking.hotel.entity.HotelSearchDocument;
import com.hrs.hotelbooking.hotel.mapper.HotelMapper;
import com.hrs.hotelbooking.hotel.repository.HotelRepository;
import com.hrs.hotelbooking.hotel.repository.HotelSearchRepository;
import com.hrs.hotelbooking.hotel.service.HotelService;
import com.hrs.hotelbooking.shared.dto.HotelDTO;
import com.hrs.hotelbooking.shared.exception.ResourceNotFoundException;
import com.hrs.hotelbooking.shared.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * HRS Hotel Service Implementation
 * Core business logic for hotel operations
 *
 * @author arihants1
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final HotelSearchRepository hotelSearchRepository;

    @Override
    public List<HotelDTO> searchHotels(String city, LocalDate checkIn, LocalDate checkOut,
                                       Integer rooms, int page, int size) {
        log.info("Searching hotels (Elasticsearch): city={}, checkIn={}, checkOut={}, rooms={}",
                city, checkIn, checkOut, rooms);

        // Use Elasticsearch for fast search by city and isActive
        List<HotelSearchDocument> docs;
        if (city != null && !city.isEmpty()) {
            docs = hotelSearchRepository.findByCityAndIsActive(city, true);
        } else {
            Iterable<HotelSearchDocument> iterable = hotelSearchRepository.findAll();
            docs = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
        }
        // Map HotelSearchDocument to HotelDTO (minimal fields)
        List<HotelDTO> results = docs.stream().map(doc -> {
            HotelDTO dto = new HotelDTO();
            dto.setId(doc.getId());
            dto.setName(doc.getName());
            dto.setCity(doc.getCity());
            dto.setCountry(doc.getCountry());
            dto.setStarRating(doc.getStarRating());
            dto.setBasePrice(doc.getBasePrice() != null ? doc.getBasePrice() : null);
            return dto;
        }).toList();
        log.info("Found {} hotels (Elasticsearch)", results.size());
        return results;
    }

    @Override
    @Cacheable(value = "hotels", key = "#id")
    public HotelDTO getHotelById(Long id) {
        log.info("Fetching hotel ID: {} ", id);

        if (id == null || id <= 0) {
            throw new BusinessValidationException("Invalid hotel ID");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        return hotelMapper.toDto(hotel);
    }

    @Override
    public Page<HotelDTO> getAllHotels(int page, int size) {
        log.info("Fetching all hotels - page: {}, size: {}", page, size);

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Hotel> hotelsPage = hotelRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);

        return hotelsPage.map(hotelMapper::toDto);
    }

    @Override
    @Cacheable(value = "hotelsByCity", key = "#city")
    public List<HotelDTO> getHotelsByCity(String city) {
        log.info("Fetching hotels in city: {} ", city);

        if (!StringUtils.hasText(city)) {
            throw new BusinessValidationException("City name is required");
        }

        Pageable pageable = PageRequest.of(0, 50); // Limit to 50
        Page<Hotel> hotelsPage = hotelRepository
                .findByCityIgnoreCaseAndIsActiveTrueOrderByStarRatingDescBasePriceAsc(city, pageable);

        return hotelMapper.toDtoList(hotelsPage.getContent());
    }

    @Override
    @Transactional
    public HotelDTO createHotel(HotelDTO hotelDTO) {
        log.info("Creating hotel: {} ", hotelDTO.getName());

        validateHotelDto(hotelDTO);

        Hotel hotel = hotelMapper.toEntity(hotelDTO);
        Hotel savedHotel = hotelRepository.save(hotel);

        // Index in Elasticsearch for fast search
        HotelSearchDocument doc = new HotelSearchDocument();
        doc.setId(savedHotel.getId());
        doc.setName(savedHotel.getName());
        doc.setCity(savedHotel.getCity());
        doc.setCountry(savedHotel.getCountry());
        doc.setStarRating(savedHotel.getStarRating());
        doc.setBasePrice(savedHotel.getBasePrice() != null ? savedHotel.getBasePrice() : null);
        doc.setIsActive(savedHotel.getIsActive());
        hotelSearchRepository.save(doc);

        return hotelMapper.toDto(savedHotel);
    }

    @Override
    @Transactional
    public HotelDTO updateHotel(Long id, HotelDTO hotelDTO) {
        log.info("Updating hotel ID: {} ", id);

        if (id == null || id <= 0) {
            throw new BusinessValidationException("Invalid hotel ID");
        }

        validateHotelDto(hotelDTO);

        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        hotelMapper.updateEntityFromDto(existingHotel, hotelDTO);
        Hotel updatedHotel = hotelRepository.save(existingHotel);

        // Update Elasticsearch index
        HotelSearchDocument doc = new HotelSearchDocument();
        doc.setId(updatedHotel.getId());
        doc.setName(updatedHotel.getName());
        doc.setCity(updatedHotel.getCity());
        doc.setCountry(updatedHotel.getCountry());
        doc.setStarRating(updatedHotel.getStarRating());
        doc.setBasePrice(updatedHotel.getBasePrice() != null ? updatedHotel.getBasePrice() : null);
        doc.setIsActive(updatedHotel.getIsActive());
        hotelSearchRepository.save(doc);

        return hotelMapper.toDto(updatedHotel);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        log.info("Deleting hotel ID: {} ", id);

        if (id == null || id <= 0) {
            throw new BusinessValidationException("Invalid hotel ID");
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        // Soft delete
        hotel.setIsActive(false);
        hotelRepository.save(hotel);

        // Remove from Elasticsearch index
        hotelSearchRepository.deleteById(id);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new BusinessValidationException("Page cannot be negative");
        }
        if (size <= 0 || size > 50) {
            throw new BusinessValidationException("Size must be between 1 and 50");
        }
    }

    private void validateHotelDto(HotelDTO hotelDTO) {
        if (hotelDTO == null) {
            throw new BusinessValidationException("Hotel information is required");
        }
        if (!StringUtils.hasText(hotelDTO.getName())) {
            throw new BusinessValidationException("Hotel name is required");
        }
        if (!StringUtils.hasText(hotelDTO.getCity())) {
            throw new BusinessValidationException("City is required");
        }
        if (!StringUtils.hasText(hotelDTO.getCountry())) {
            throw new BusinessValidationException("Country is required");
        }
    }
}