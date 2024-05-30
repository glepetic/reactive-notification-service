package com.challenge.ratelimit.adapter;

import com.challenge.ratelimit.domain.model.RateLimitConfig;
import com.challenge.ratelimit.infrastructure.dto.entity.RateLimitConfigEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimitAdapter {

    public RateLimitConfig toModel(final RateLimitConfigEntity entity) {
        return new RateLimitConfig(entity.maxCount(), Duration.of(entity.rate(), entity.unit()));
    }

}
