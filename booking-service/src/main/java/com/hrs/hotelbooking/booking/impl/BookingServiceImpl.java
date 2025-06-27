package com.hrs.hotelbooking.booking.service.impl;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.booking.mapper.BookingMapper;
import com.hrs.hotelbooking.booking.repository.BookingRepository;
import com.hrs.hotelbooking.booking.service.BookingService;
import com.hrs.hotelbooking.shared.dto.BookingDTO;
import com.hrs.hotelbooking.shared.dto.BookingStatus;
import com.hrs.hotelbooking.shared.exception.ResourceNotFoundException;
import com.hrs.hotelbooking.shared.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HRS Booking Service Implementation - Essential Features Only
 * Core business logic for booking operations in the HRS booking system
 * Simplified implementation focusing on essential functionality
 *
 * @author arihants1
 * @since 2025-06-27 05:47:04 UTC
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    private static final String CURRENT_USER = "arihants1";
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05"); // 5% service fee
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    @CacheEvict(value = {"bookings", "userBookings"}, allEntries = true)
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        log.info("Creating HRS booking at 2025-06-27 05:47:04 by {} for user: {} hotel: {}",
                CURRENT_USER, bookingDTO.getUserId(), bookingDTO.getHotelId());

        // Essential validation
        validateBookingDto(bookingDTO);
        validateBookingDates(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        // Check for duplicate bookings
        checkDuplicateBookings(bookingDTO);

        // Generate unique identifiers
        String bookingReference = generateBookingReference();
        String confirmationNumber = generateConfirmationNumber();

        // Calculate simple pricing
        calculatePricing(bookingDTO);

        // Create booking entity
        Booking booking = bookingMapper.toEntity(bookingDTO);
        booking.setBookingReference(bookingReference);
        booking.setConfirmationNumber(confirmationNumber);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus("PENDING");

        // Set default guest info if not provided
        setDefaultGuestInfo(booking, bookingDTO);

        Booking savedBooking = bookingRepository.save(booking);

        BookingDTO result = bookingMapper.toDto(savedBooking);
        log.info("Successfully created HRS booking with reference: {} at 2025-06-27 05:47:04", bookingReference);

        return result;
    }

    @Override
    @Cacheable(value = "bookings", key = "#id")
    public BookingDTO getBookingById(Long id) {
        log.info("Fetching HRS booking with ID: {} at 2025-06-27 05:47:04 by {}", id, CURRENT_USER);

        validateBookingId(id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HRS Booking", "id", id));

        BookingDTO result = bookingMapper.toDto(booking);
        log.debug("Successfully retrieved HRS booking: {} at 2025-06-27 05:47:04", result.getBookingReference());

        return result;
    }

    @Override
    @Cacheable(value = "bookings", key = "#bookingReference")
    public BookingDTO getBookingByReference(String bookingReference) {
        log.info("Fetching HRS booking with reference: {} at 2025-06-27 05:47:04 by {}", bookingReference, CURRENT_USER);

        validateBookingReference(bookingReference);

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("HRS Booking", "reference", bookingReference));

        BookingDTO result = bookingMapper.toDto(booking);
        log.debug("Successfully retrieved HRS booking by reference at 2025-06-27 05:47:04");

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"bookings", "userBookings"}, allEntries = true)
    public BookingDTO updateBooking(Long id, BookingDTO bookingDTO) {
        log.info("Updating HRS booking with ID: {} at 2025-06-27 05:47:04 by {}", id, CURRENT_USER);

        validateBookingId(id);

        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HRS Booking", "id", id));

        // Validate that booking can be updated
        if (!existingBooking.isModifiable()) {
            throw new BusinessValidationException("Booking cannot be modified in current status");
        }

        // Validate new booking data if dates are being changed
        if (bookingDTO.getCheckInDate() != null && bookingDTO.getCheckOutDate() != null) {
            validateBookingDates(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        }

        // Check for duplicate bookings if dates changed
        if (datesChanged(existingBooking, bookingDTO)) {
            checkDuplicateBookings(bookingDTO, id);
        }

        // Update fields using mapper
        bookingMapper.updateEntityFromDto(existingBooking, bookingDTO);

        // Recalculate pricing if needed
        if (isPricingRecalculationNeeded(existingBooking, bookingDTO)) {
            recalculatePricing(existingBooking);
        }

        Booking updatedBooking = bookingRepository.save(existingBooking);
        BookingDTO result = bookingMapper.toDto(updatedBooking);

        log.info("Successfully updated HRS booking: {} at 2025-06-27 05:47:04", result.getBookingReference());

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"bookings", "userBookings"}, allEntries = true)
    public BookingDTO cancelBooking(Long id) {
        log.info("Cancelling HRS booking with ID: {} at 2025-06-27 05:47:04 by {}", id, CURRENT_USER);

        validateBookingId(id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HRS Booking", "id", id));

        // Validate that booking can be cancelled
        if (!booking.isCancellable()) {
            throw new BusinessValidationException("Booking cannot be cancelled in current status");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(CURRENT_USER);
        booking.setCancellationReason("Customer request");

        Booking cancelledBooking = bookingRepository.save(booking);
        BookingDTO result = bookingMapper.toDto(cancelledBooking);

        log.info("Successfully cancelled HRS booking: {} at 2025-06-27 05:47:04", result.getBookingReference());

        return result;
    }

    @Override
    @Cacheable(value = "userBookings", key = "#userId")
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        log.info("Fetching HRS bookings for user: {} at 2025-06-27 05:47:04 by {}", userId, CURRENT_USER);

        validateUserId(userId);

        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<BookingDTO> result = bookingMapper.toDtoList(bookings);

        log.debug("Found {} HRS bookings for user {} at 2025-06-27 05:47:04", result.size(), userId);

        return result;
    }

    @Override
    public List<BookingDTO> getBookingsByHotelId(Long hotelId) {
        log.info("Fetching HRS bookings for hotel: {} at 2025-06-27 05:47:04 by {}", hotelId, CURRENT_USER);

        validateHotelId(hotelId);

        List<Booking> bookings = bookingRepository.findByHotelIdOrderByCheckInDateAsc(hotelId);
        List<BookingDTO> result = bookingMapper.toDtoList(bookings);

        log.debug("Found {} HRS bookings for hotel {} at 2025-06-27 05:47:04", result.size(), hotelId);

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"bookings", "userBookings"}, allEntries = true)
    public BookingDTO checkInGuest(Long bookingId) {
        log.info("Checking in guest for HRS booking: {} at 2025-06-27 05:47:04 by {}", bookingId, CURRENT_USER);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("HRS Booking", "id", bookingId));

        // Validate check-in is allowed
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessValidationException("Only confirmed bookings can be checked in");
        }

        // Check if check-in date is today or past
        LocalDate today = LocalDate.now();
        if (booking.getCheckInDate().isAfter(today)) {
            throw new BusinessValidationException("Cannot check in before check-in date");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(LocalDateTime.now());

        Booking checkedInBooking = bookingRepository.save(booking);
        BookingDTO result = bookingMapper.toDto(checkedInBooking);

        log.info("Successfully checked in guest for booking: {} at 2025-06-27 05:47:04", result.getBookingReference());

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"bookings", "userBookings"}, allEntries = true)
    public BookingDTO checkOutGuest(Long bookingId) {
        log.info("Checking out guest for HRS booking: {} at 2025-06-27 05:47:04 by {}", bookingId, CURRENT_USER);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("HRS Booking", "id", bookingId));

        // Validate check-out is allowed
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new BusinessValidationException("Only checked-in bookings can be checked out");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        booking.setCheckedOutAt(LocalDateTime.now());

        Booking checkedOutBooking = bookingRepository.save(booking);
        BookingDTO result = bookingMapper.toDto(checkedOutBooking);

        log.info("Successfully checked out guest for booking: {} at 2025-06-27 05:47:04", result.getBookingReference());

        return result;
    }

    @Override
    public Page<BookingDTO> getAllBookings(int page, int size) {
        log.info("Fetching all HRS bookings at 2025-06-27 05:47:04 by {} - page: {}, size: {}", CURRENT_USER, page, size);

        validatePaginationParameters(page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingsPage = bookingRepository.findAll(pageable);

        Page<BookingDTO> result = bookingsPage.map(bookingMapper::toDto);
        log.debug("Retrieved {} HRS bookings at 2025-06-27 05:47:04", result.getContent().size());

        return result;
    }

    // Private helper methods
    private String generateBookingReference() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String randomSuffix = String.format("%04d", (int)(Math.random() * 10000));
        String reference = "HRS_" + timestamp + "_" + randomSuffix;

        // Ensure uniqueness
        int attempts = 0;
        while (bookingRepository.existsByBookingReference(reference) && attempts < 5) {
            randomSuffix = String.format("%04d", (int)(Math.random() * 10000));
            reference = "HRS_" + timestamp + "_" + randomSuffix;
            attempts++;
        }

        return reference;
    }

    private String generateConfirmationNumber() {
        String prefix = "CONF";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%06d", (int)(Math.random() * 1000000));
        String confirmation = prefix + timestamp + randomSuffix;

        // Ensure uniqueness
        int attempts = 0;
        while (bookingRepository.existsByConfirmationNumber(confirmation) && attempts < 5) {
            randomSuffix = String.format("%06d", (int)(Math.random() * 1000000));
            confirmation = prefix + timestamp + randomSuffix;
            attempts++;
        }

        return confirmation;
    }

    private void calculatePricing(BookingDTO bookingDTO) {
        // Simple pricing calculation
        BigDecimal basePrice = new BigDecimal("100.00"); // Default base price per night per room
        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        BigDecimal baseAmount = basePrice
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(bookingDTO.getNumberOfRooms()));

        // Apply discount if any
        BigDecimal discountAmount = bookingDTO.getDiscountAmount() != null ?
                bookingDTO.getDiscountAmount() : BigDecimal.ZERO;

        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            baseAmount = baseAmount.subtract(discountAmount);
            if (baseAmount.compareTo(BigDecimal.ZERO) < 0) {
                baseAmount = BigDecimal.ZERO;
            }
        }

        // Calculate taxes and fees
        BigDecimal taxesAmount = baseAmount.multiply(TAX_RATE);
        BigDecimal feesAmount = baseAmount.multiply(SERVICE_FEE_RATE);
        BigDecimal totalAmount = baseAmount.add(taxesAmount).add(feesAmount);

        // Set calculated amounts
        bookingDTO.setBaseAmount(baseAmount);
        bookingDTO.setTaxesAmount(taxesAmount);
        bookingDTO.setFeesAmount(feesAmount);
        bookingDTO.setTotalAmount(totalAmount);

        log.debug("Calculated booking pricing at 2025-06-27 05:47:04 - Base: {}, Taxes: {}, Fees: {}, Total: {}",
                baseAmount, taxesAmount, feesAmount, totalAmount);
    }

    private void recalculatePricing(Booking booking) {
        BookingDTO tempDto = BookingDTO.builder()
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .numberOfRooms(booking.getNumberOfRooms())
                .discountAmount(booking.getDiscountAmount())
                .build();

        calculatePricing(tempDto);

        booking.setBaseAmount(tempDto.getBaseAmount());
        booking.setTaxesAmount(tempDto.getTaxesAmount());
        booking.setFeesAmount(tempDto.getFeesAmount());
        booking.setTotalAmount(tempDto.getTotalAmount());
    }

    private void setDefaultGuestInfo(Booking booking, BookingDTO bookingDTO) {
        if (!StringUtils.hasText(booking.getGuestName()) && StringUtils.hasText(bookingDTO.getGuestName())) {
            booking.setGuestName(bookingDTO.getGuestName());
        }
        if (!StringUtils.hasText(booking.getGuestEmail()) && StringUtils.hasText(bookingDTO.getGuestEmail())) {
            booking.setGuestEmail(bookingDTO.getGuestEmail());
        }
        if (!StringUtils.hasText(booking.getGuestPhone()) && StringUtils.hasText(bookingDTO.getGuestPhone())) {
            booking.setGuestPhone(bookingDTO.getGuestPhone());
        }

        // Set default guest name if not provided
        if (!StringUtils.hasText(booking.getGuestName())) {
            booking.setGuestName("Guest " + booking.getUserId());
        }
    }

    private void checkDuplicateBookings(BookingDTO bookingDTO) {
        checkDuplicateBookings(bookingDTO, null);
    }

    private void checkDuplicateBookings(BookingDTO bookingDTO, Long excludeId) {
        List<Booking> duplicates = bookingRepository.findDuplicateBookings(
                bookingDTO.getUserId(),
                bookingDTO.getHotelId(),
                bookingDTO.getCheckInDate(),
                bookingDTO.getCheckOutDate(),
                excludeId
        );

        if (!duplicates.isEmpty()) {
            throw new BusinessValidationException(
                    "You already have a booking for this hotel with overlapping dates. Reference: " +
                            duplicates.get(0).getBookingReference());
        }
    }

    private boolean datesChanged(Booking existingBooking, BookingDTO bookingDTO) {
        return (bookingDTO.getCheckInDate() != null &&
                !bookingDTO.getCheckInDate().equals(existingBooking.getCheckInDate())) ||
                (bookingDTO.getCheckOutDate() != null &&
                        !bookingDTO.getCheckOutDate().equals(existingBooking.getCheckOutDate()));
    }

    private boolean isPricingRecalculationNeeded(Booking existingBooking, BookingDTO bookingDTO) {
        return datesChanged(existingBooking, bookingDTO) ||
                (bookingDTO.getNumberOfRooms() != null &&
                        !bookingDTO.getNumberOfRooms().equals(existingBooking.getNumberOfRooms())) ||
                (bookingDTO.getDiscountAmount() != null &&
                        !bookingDTO.getDiscountAmount().equals(existingBooking.getDiscountAmount()));
    }

    // Validation methods
    private void validateBookingDto(BookingDTO bookingDTO) {
        if (bookingDTO == null) {
            throw new BusinessValidationException("Booking information is required");
        }
        if (bookingDTO.getUserId() == null || bookingDTO.getUserId() <= 0) {
            throw new BusinessValidationException("Valid user ID is required");
        }
        if (bookingDTO.getHotelId() == null || bookingDTO.getHotelId() <= 0) {
            throw new BusinessValidationException("Valid hotel ID is required");
        }
        if (bookingDTO.getCheckInDate() == null) {
            throw new BusinessValidationException("Check-in date is required");
        }
        if (bookingDTO.getCheckOutDate() == null) {
            throw new BusinessValidationException("Check-out date is required");
        }
        if (bookingDTO.getNumberOfRooms() == null || bookingDTO.getNumberOfRooms() <= 0) {
            throw new BusinessValidationException("Number of rooms must be at least 1");
        }
        if (bookingDTO.getNumberOfRooms() > 5) {
            throw new BusinessValidationException("Cannot book more than 5 rooms at once");
        }
        if (bookingDTO.getNumberOfGuests() == null || bookingDTO.getNumberOfGuests() <= 0) {
            throw new BusinessValidationException("Number of guests must be at least 1");
        }
        if (bookingDTO.getNumberOfGuests() > (bookingDTO.getNumberOfRooms() * 4)) {
            throw new BusinessValidationException("Too many guests for the number of rooms (max 4 per room)");
        }
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BusinessValidationException("Check-in and check-out dates are required");
        }

        LocalDate today = LocalDate.now();

        if (checkIn.isBefore(today)) {
            throw new BusinessValidationException("Check-in date cannot be in the past");
        }

        if (checkOut.isBefore(checkIn.plusDays(1))) {
            throw new BusinessValidationException("Check-out date must be at least one day after check-in");
        }

        if (checkIn.isAfter(today.plusYears(1))) {
            throw new BusinessValidationException("Check-in date cannot be more than 1 year in the future");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights > 30) {
            throw new BusinessValidationException("Booking cannot exceed 30 nights");
        }
    }

    private void validateBookingId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessValidationException("Invalid booking ID provided");
        }
    }

    private void validateBookingReference(String reference) {
        if (!StringUtils.hasText(reference)) {
            throw new BusinessValidationException("Booking reference cannot be empty");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessValidationException("Invalid user ID provided");
        }
    }

    private void validateHotelId(Long hotelId) {
        if (hotelId == null || hotelId <= 0) {
            throw new BusinessValidationException("Invalid hotel ID provided");
        }
    }

    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new BusinessValidationException("Page number cannot be negative");
        }
        if (size <= 0 || size > 50) {
            throw new BusinessValidationException("Page size must be between 1 and 50");
        }
    }
}