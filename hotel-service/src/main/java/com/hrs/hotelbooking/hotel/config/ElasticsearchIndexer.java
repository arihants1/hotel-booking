package com.hrs.hotelbooking.hotel.config;

import com.hrs.hotelbooking.hotel.entity.Hotel;
import com.hrs.hotelbooking.hotel.entity.HotelSearchDocument;
import com.hrs.hotelbooking.hotel.repository.HotelRepository;
import com.hrs.hotelbooking.hotel.repository.HotelSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch Indexer - Synchronizes PostgreSQL hotels data to Elasticsearch on application startup
 * Ensures search index is populated and in sync with the database after init.sql executes
 * Uses pagination to handle large datasets efficiently
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchIndexer implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final HotelSearchRepository hotelSearchRepository;

    @Value("${hrs.elasticsearch.batch-size:100}")
    private int batchSize;

    @Override
    public void run(String... args) {
        log.info("Starting Elasticsearch indexer - synchronizing hotels from PostgreSQL to Elasticsearch...");

        int newCount = 0;
        int updatedCount = 0;
        int pageNumber = 0;
        int totalProcessed = 0;
        long start = System.currentTimeMillis();

        // Get total count for progress tracking
        long totalCount = hotelRepository.count();
        log.info("Found {} total hotels in database to process", totalCount);

        if (totalCount == 0) {
            log.warn("No hotels found in database. Elasticsearch index will be empty.");
            return;
        }

        // Process hotels in batches using pagination
        Page<Hotel> hotelPage;
        do {
            Pageable pageable = PageRequest.of(pageNumber, batchSize);
            hotelPage = hotelRepository.findAll(pageable);

            List<HotelSearchDocument> batch = new ArrayList<>();

            for (Hotel hotel : hotelPage.getContent()) {
                boolean exists = hotelSearchRepository.existsById(hotel.getId());

                HotelSearchDocument doc = new HotelSearchDocument();
                doc.setId(hotel.getId());
                doc.setName(hotel.getName());
                doc.setCity(hotel.getCity());
                doc.setCountry(hotel.getCountry());
                doc.setStarRating(hotel.getStarRating());
                doc.setBasePrice(hotel.getBasePrice() != null ? hotel.getBasePrice() : null);
                doc.setIsActive(hotel.getIsActive());
                batch.add(doc);

                if (exists) {
                    updatedCount++;
                } else {
                    newCount++;
                }
            }

            // Bulk save the batch
            if (!batch.isEmpty()) {
                hotelSearchRepository.saveAll(batch);
            }

            totalProcessed += hotelPage.getNumberOfElements();
            log.info("Processed {}/{} hotels ({}%)",
                    totalProcessed,
                    totalCount,
                    Math.round((double) totalProcessed / totalCount * 100));

            pageNumber++;
        } while (hotelPage.hasNext());

        long duration = System.currentTimeMillis() - start;
        log.info("Elasticsearch indexing complete: {} new hotels indexed, {} existing hotels updated in {}ms",
                newCount, updatedCount, duration);
    }
}
