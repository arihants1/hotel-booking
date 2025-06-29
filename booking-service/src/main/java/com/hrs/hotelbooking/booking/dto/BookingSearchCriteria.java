package com.hrs.hotelbooking.booking.dto;

import com.hrs.hotelbooking.shared.dto.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Search criteria for advanced booking searches
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSearchCriteria {

    private List<BookingStatus> statuses;
    private LocalDate checkInFrom;
    private LocalDate checkInTo;
    private LocalDate checkOutFrom;
    private LocalDate checkOutTo;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String hotelCity;
    private String hotelCountry;
    private List<String> roomTypes;
    private Boolean isUpcoming;
    private Boolean isPast;
    private String paymentStatus;
}
