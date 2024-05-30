package com.challenge.ratelimit.domain.model;

import java.time.Duration;

public record RateLimitConfig(Integer maxCount,
                              Duration rate) {
}
