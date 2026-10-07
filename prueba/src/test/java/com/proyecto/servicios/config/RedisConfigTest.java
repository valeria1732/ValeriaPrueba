package com.proyecto.servicios.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    @DisplayName("Debe construir RedisTemplate y RedisCacheManager correctamente")
    void testRedisBeansCreation() {
        RedisConfig config = new RedisConfig();
        ReflectionTestUtils.setField(config, "timeToLiveMs", 1800000L);

        RedisTemplate<String, Object> template = config.redisTemplate(redisConnectionFactory);
        assertNotNull(template);

        RedisCacheManager cacheManager = config.cacheManager(redisConnectionFactory);
        assertNotNull(cacheManager);
    }

    @Test
    @DisplayName("CacheErrorHandler resiliente no debe propagar excepciones cuando Redis falla")
    void testCacheErrorHandlerResilience() {
        RedisConfig config = new RedisConfig();
        CacheErrorHandler errorHandler = config.errorHandler();
        assertNotNull(errorHandler);

        Cache mockCache = mock(Cache.class);
        RuntimeException ex = new RuntimeException("Redis connection timeout");

        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(ex, mockCache, "key-123"));
        assertDoesNotThrow(() -> errorHandler.handleCachePutError(ex, mockCache, "key-123", "value-abc"));
        assertDoesNotThrow(() -> errorHandler.handleCacheEvictError(ex, mockCache, "key-123"));
        assertDoesNotThrow(() -> errorHandler.handleCacheClearError(ex, mockCache));
    }
}
