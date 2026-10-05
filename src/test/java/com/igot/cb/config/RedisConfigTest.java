package com.igot.cb.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;
import com.igot.cb.model.ResponseDTO;

import static org.assertj.core.api.Assertions.assertThat;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(redisConfig, "redisHost", "localhost");
        ReflectionTestUtils.setField(redisConfig, "redisPort", 6379);
        ReflectionTestUtils.setField(redisConfig, "redisDatabase", 1);
    }

    @Test
    void shouldCreateReactiveRedisConnectionFactory() {
        ReactiveRedisConnectionFactory factory = redisConfig.reactiveRedisConnectionFactory();

        assertThat(factory).isInstanceOf(LettuceConnectionFactory.class);
    }

    @Test
    void shouldCreateReactiveRedisTemplate() {
        ReactiveRedisConnectionFactory factory = redisConfig.reactiveRedisConnectionFactory();

        ReactiveRedisTemplate<String, ResponseDTO> template = redisConfig.redisOperations(factory);

        assertThat(template).isNotNull();
    }
}
