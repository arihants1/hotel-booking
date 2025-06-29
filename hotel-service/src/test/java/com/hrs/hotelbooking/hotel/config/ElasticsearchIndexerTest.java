package com.hrs.hotelbooking.hotel.config;

import com.hrs.hotelbooking.hotel.entity.Hotel;
import com.hrs.hotelbooking.hotel.entity.HotelSearchDocument;
import com.hrs.hotelbooking.hotel.repository.HotelRepository;
import com.hrs.hotelbooking.hotel.repository.HotelSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class ElasticsearchIndexerTest {
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private HotelSearchRepository hotelSearchRepository;
    @InjectMocks
    private ElasticsearchIndexer elasticsearchIndexer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(elasticsearchIndexer, "batchSize", 2);
    }

    @Test
    void run_shouldDoNothingIfNoHotels() {
        when(hotelRepository.count()).thenReturn(0L);
        elasticsearchIndexer.run();
        verify(hotelRepository).count();
        verifyNoMoreInteractions(hotelRepository, hotelSearchRepository);
    }

    @Test
    void run_shouldIndexHotelsInBatches() {
        when(hotelRepository.count()).thenReturn(3L);
        Hotel h1 = new Hotel(); h1.setId(1L); h1.setName("Hotel1");
        Hotel h2 = new Hotel(); h2.setId(2L); h2.setName("Hotel2");
        Hotel h3 = new Hotel(); h3.setId(3L); h3.setName("Hotel3");
        Page<Hotel> hotelsPage1 = new PageImpl<>(Arrays.asList(h1, h2));
        Page<Hotel> hotelsPage2 = new PageImpl<>(Collections.singletonList(h3));
        when(hotelRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(hotelsPage1, hotelsPage2);
        when(hotelSearchRepository.saveAll(anyList())).thenReturn(null);
        elasticsearchIndexer.run();
        verify(hotelRepository, atLeastOnce()).findAll(any(org.springframework.data.domain.Pageable.class));
        verify(hotelSearchRepository, atLeastOnce()).saveAll(anyList());
    }
}
