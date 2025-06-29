package com.hrs.hotelbooking.booking.entity;

import com.hrs.hotelbooking.shared.dto.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Booking Search Document for Elasticsearch
 * Optimized for searching and filtering booking data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "bookings")
@Setting(settingPath = "/elasticsearch/booking-settings.json")
public class BookingSearchDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long hotelId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String hotelName;

    @Field(type = FieldType.Keyword)
    private String hotelCity;

    @Field(type = FieldType.Keyword)
    private String hotelCountry;

    @Field(type = FieldType.Date)
    private LocalDate checkInDate;

    @Field(type = FieldType.Date)
    private LocalDate checkOutDate;

    @Field(type = FieldType.Keyword)
    private String roomType;

    @Field(type = FieldType.Integer)
    private Integer numberOfRooms;

    @Field(type = FieldType.Integer)
    private Integer numberOfGuests;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Field(type = FieldType.Double)
    private BigDecimal baseAmount;

    @Field(type = FieldType.Double)
    private BigDecimal taxesAmount;

    @Field(type = FieldType.Double)
    private BigDecimal feesAmount;

    @Field(type = FieldType.Keyword)
    private BookingStatus status;

    @Field(type = FieldType.Keyword)
    private String bookingReference;

    @Field(type = FieldType.Keyword)
    private String confirmationNumber;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String specialRequests;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String guestName;

    @Field(type = FieldType.Keyword)
    private String guestEmail;

    @Field(type = FieldType.Keyword)
    private String guestPhone;

    @Field(type = FieldType.Keyword)
    private String paymentStatus;

    @Field(type = FieldType.Keyword)
    private String paymentMethod;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime cancelledAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime checkedInAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime checkedOutAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    // Derived fields for better search capabilities
    @Field(type = FieldType.Integer)
    private Integer stayDuration;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Boolean)
    private Boolean isUpcoming;

    @Field(type = FieldType.Boolean)
    private Boolean isPast;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    // Combined field for full-text search
    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchableText;
}
