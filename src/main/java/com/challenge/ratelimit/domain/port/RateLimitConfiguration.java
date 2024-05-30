package com.challenge.ratelimit.domain.port;

import com.challenge.ratelimit.domain.model.RateLimitConfig;
import reactor.core.publisher.Mono;

public interface RateLimitConfiguration<T> {
    Mono<RateLimitConfig> getRateLimitConfig(final T type);
}
