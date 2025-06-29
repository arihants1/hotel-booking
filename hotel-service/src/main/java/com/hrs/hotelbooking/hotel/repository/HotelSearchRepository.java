package com.hrs.hotelbooking.hotel.repository;

import com.hrs.hotelbooking.hotel.entity.HotelSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelSearchRepository extends ElasticsearchRepository<HotelSearchDocument, Long> {
    List<HotelSearchDocument> findByCityAndIsActive(String city, boolean isActive);
    List<HotelSearchDocument> findByNameContainingIgnoreCaseAndIsActive(String name, boolean isActive);
    // Add more custom search methods as needed
}


