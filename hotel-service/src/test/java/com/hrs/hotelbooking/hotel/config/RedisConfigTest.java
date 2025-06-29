package com.hrs.hotelbooking.hotel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RedisConfig.class)
class RedisConfigTest {
    @Autowired
    private ApplicationContext context;

    @Autowired
    private ObjectMapper redisObjectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void redisObjectMapperBeanExists() {
        assertThat(redisObjectMapper).isNotNull();
        // Should have JavaTimeModule registered
        assertThat(redisObjectMapper.findModules()).anyMatch(m -> m.getClass().getSimpleName().equals("JavaTimeModule"));
    }

    @Test
    void redisTemplateBeanExists() {
        assertThat(redisTemplate).isNotNull();
        assertThat(redisTemplate.getConnectionFactory()).isEqualTo(redisConnectionFactory);
    }

    @Test
    void cacheManagerBeanExists() {
        assertThat(cacheManager).isNotNull();
    }
}
