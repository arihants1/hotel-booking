package com.hrs.hotelbooking.booking.mapper;

import com.hrs.hotelbooking.booking.entity.Booking;
import com.hrs.hotelbooking.shared.dto.BookingDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HRS Booking Mapper - Essential Mappings Only
 * Converts between Booking entity and BookingDTO for simplified operations
 *
 * @author arihants1
 * @since 2025-06-27 05:47:04 UTC
 */
@Component
@Slf4j
public class BookingMapper {

    /**
     * Convert Booking entity to BookingDTO
     */
    public BookingDTO toDto(Booking booking) {
        if (booking == null) {
            log.warn("Attempting to convert null Booking entity to DTO at 2025-06-27 05:47:04");
            return null;
        }

        try {
            BookingDTO dto = BookingDTO.builder()
                    .id(booking.getId())
                    .userId(booking.getUserId())
                    .hotelId(booking.getHotelId())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .roomType(booking.getRoomType())
                    .numberOfRooms(booking.getNumberOfRooms())
                    .numberOfGuests(booking.getNumberOfGuests())
                    .totalAmount(booking.getTotalAmount())
                    .baseAmount(booking.getBaseAmount())
                    .taxesAmount(booking.getTaxesAmount())
                    .feesAmount(booking.getFeesAmount())
                    .status(booking.getStatus())
                    .bookingReference(booking.getBookingReference())
                    .confirmationNumber(booking.getConfirmationNumber())
                    .specialRequests(booking.getSpecialRequests())
                    .guestName(booking.getGuestName())
                    .guestEmail(booking.getGuestEmail())
                    .guestPhone(booking.getGuestPhone())
                    .paymentStatus(booking.getPaymentStatus())
                    .paymentMethod(booking.getPaymentMethod())
                    .paymentReference(booking.getPaymentReference())
                    .cancellationReason(booking.getCancellationReason())
                    .cancelledAt(booking.getCancelledAt())
                    .cancelledBy(booking.getCancelledBy())
                    .checkedInAt(booking.getCheckedInAt())
                    .checkedOutAt(booking.getCheckedOutAt())
                    .discountAmount(booking.getDiscountAmount())
                    .createdAt(booking.getCreatedAt())
                    .updatedAt(booking.getUpdatedAt())
                    .build();

            // Set calculated fields using entity methods
            if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                dto.setNights((int) booking.calculateNights());
            }
            dto.setModifiable(booking.isModifiable());
            dto.setCancellable(booking.isCancellable());

            // Set refund amount if cancelled
            if (booking.getStatus() == com.hrs.hotelbooking.shared.dto.BookingStatus.CANCELLED) {
                dto.setRefundAmount(booking.calculateRefundAmount());
            }

            return dto;
        } catch (Exception e) {
            log.error("Error converting Booking entity {} to DTO at 2025-06-27 05:47:04: {}",
                    booking.getId(), e.getMessage());
            throw new RuntimeException("Failed to convert Booking to DTO", e);
        }
    }

    /**
     * Convert BookingDTO to Booking entity
     */
    public Booking toEntity(BookingDTO bookingDTO) {
        if (bookingDTO == null) {
            log.warn("Attempting to convert null BookingDTO to entity at 2025-06-27 05:47:04");
            return null;
        }

        try {
            return Booking.builder()
                    .id(bookingDTO.getId())
                    .userId(bookingDTO.getUserId())
                    .hotelId(bookingDTO.getHotelId())
                    .checkInDate(bookingDTO.getCheckInDate())
                    .checkOutDate(bookingDTO.getCheckOutDate())
                    .roomType(bookingDTO.getRoomType())
                    .numberOfRooms(bookingDTO.getNumberOfRooms())
                    .numberOfGuests(bookingDTO.getNumberOfGuests())
                    .totalAmount(bookingDTO.getTotalAmount())
                    .baseAmount(bookingDTO.getBaseAmount())
                    .taxesAmount(bookingDTO.getTaxesAmount())
                    .feesAmount(bookingDTO.getFeesAmount())
                    .status(bookingDTO.getStatus())
                    .bookingReference(bookingDTO.getBookingReference())
                    .confirmationNumber(bookingDTO.getConfirmationNumber())
                    .specialRequests(bookingDTO.getSpecialRequests())
                    .guestName(bookingDTO.getGuestName())
                    .guestEmail(bookingDTO.getGuestEmail())
                    .guestPhone(bookingDTO.getGuestPhone())
                    .paymentStatus(bookingDTO.getPaymentStatus())
                    .paymentMethod(bookingDTO.getPaymentMethod())
                    .paymentReference(bookingDTO.getPaymentReference())
                    .discountAmount(bookingDTO.getDiscountAmount())
                    .createdAt(bookingDTO.getCreatedAt())
                    .updatedAt(bookingDTO.getUpdatedAt())
                    .createdBy("arihants1")
                    .updatedBy("arihants1")
                    .build();
        } catch (Exception e) {
            log.error("Error converting BookingDTO {} to entity at 2025-06-27 05:47:04: {}",
                    bookingDTO.getBookingReference(), e.getMessage());
            throw new RuntimeException("Failed to convert BookingDTO to entity", e);
        }
    }

    /**
     * Convert list of Booking entities to BookingDTO list
     */
    public List<BookingDTO> toDtoList(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            log.debug("Converting empty or null booking list to DTOs at 2025-06-27 05:47:04");
            return List.of();
        }

        try {
            List<BookingDTO> result = bookings.stream()
                    .map(this::toDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            log.debug("Successfully converted {} bookings to DTOs at 2025-06-27 05:47:04", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error converting booking list to DTOs at 2025-06-27 05:47:04: {}", e.getMessage());
            throw new RuntimeException("Failed to convert booking list to DTOs", e);
        }
    }

    /**
     * Update existing Booking entity with BookingDTO data
     */
    public void updateEntityFromDto(Booking existingBooking, BookingDTO bookingDTO) {
        if (existingBooking == null || bookingDTO == null) {
            log.warn("Cannot update booking entity: booking or DTO is null at 2025-06-27 05:47:04");
            return;
        }

        try {
            if (bookingDTO.getCheckInDate() != null) {
                existingBooking.setCheckInDate(bookingDTO.getCheckInDate());
            }
            if (bookingDTO.getCheckOutDate() != null) {
                existingBooking.setCheckOutDate(bookingDTO.getCheckOutDate());
            }
            if (bookingDTO.getRoomType() != null) {
                existingBooking.setRoomType(bookingDTO.getRoomType());
            }
            if (bookingDTO.getNumberOfRooms() != null) {
                existingBooking.setNumberOfRooms(bookingDTO.getNumberOfRooms());
            }
            if (bookingDTO.getNumberOfGuests() != null) {
                existingBooking.setNumberOfGuests(bookingDTO.getNumberOfGuests());
            }
            if (bookingDTO.getTotalAmount() != null) {
                existingBooking.setTotalAmount(bookingDTO.getTotalAmount());
            }
            if (bookingDTO.getBaseAmount() != null) {
                existingBooking.setBaseAmount(bookingDTO.getBaseAmount());
            }
            if (bookingDTO.getTaxesAmount() != null) {
                existingBooking.setTaxesAmount(bookingDTO.getTaxesAmount());
            }
            if (bookingDTO.getFeesAmount() != null) {
                existingBooking.setFeesAmount(bookingDTO.getFeesAmount());
            }
            if (bookingDTO.getSpecialRequests() != null) {
                existingBooking.setSpecialRequests(bookingDTO.getSpecialRequests());
            }
            if (bookingDTO.getGuestName() != null) {
                existingBooking.setGuestName(bookingDTO.getGuestName());
            }
            if (bookingDTO.getGuestEmail() != null) {
                existingBooking.setGuestEmail(bookingDTO.getGuestEmail());
            }
            if (bookingDTO.getGuestPhone() != null) {
                existingBooking.setGuestPhone(bookingDTO.getGuestPhone());
            }
            if (bookingDTO.getPaymentMethod() != null) {
                existingBooking.setPaymentMethod(bookingDTO.getPaymentMethod());
            }
            if (bookingDTO.getDiscountAmount() != null) {
                existingBooking.setDiscountAmount(bookingDTO.getDiscountAmount());
            }

            // Always update the updatedBy field
            existingBooking.setUpdatedBy("arihants1");

            log.debug("Successfully updated booking entity {} from DTO at 2025-06-27 05:47:04", existingBooking.getId());
        } catch (Exception e) {
            log.error("Error updating booking entity {} from DTO at 2025-06-27 05:47:04: {}",
                    existingBooking.getId(), e.getMessage());
            throw new RuntimeException("Failed to update booking entity from DTO", e);
        }
    }

    /**
     * Create booking summary DTO (lightweight version)
     */
    public BookingDTO toSummaryDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        try {
            return BookingDTO.builder()
                    .id(booking.getId())
                    .bookingReference(booking.getBookingReference())
                    .hotelId(booking.getHotelId())
                    .userId(booking.getUserId())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .numberOfRooms(booking.getNumberOfRooms())
                    .numberOfGuests(booking.getNumberOfGuests())
                    .totalAmount(booking.getTotalAmount())
                    .status(booking.getStatus())
                    .nights((int) booking.calculateNights())
                    .modifiable(booking.isModifiable())
                    .cancellable(booking.isCancellable())
                    .createdAt(booking.getCreatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error converting Booking entity {} to summary DTO at 2025-06-27 05:47:04: {}",
                    booking.getId(), e.getMessage());
            throw new RuntimeException("Failed to convert Booking to summary DTO", e);
        }
    }
}