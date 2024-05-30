package com.challenge.ratelimit.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(final ReactiveRedisConnectionFactory connectionFactory,
                                                                       final ObjectMapper objectMapper) {

        RedisSerializationContext<String, Object> context = RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(RedisSerializer.string())
                .value(new GenericJackson2JsonRedisSerializer(objectMapper))
                .hashKey(RedisSerializer.string())
                .hashValue(new GenericJackson2JsonRedisSerializer(objectMapper))
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

}
