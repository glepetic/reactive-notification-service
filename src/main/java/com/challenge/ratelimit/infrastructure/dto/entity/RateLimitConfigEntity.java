package com.challenge.ratelimit.infrastructure.dto.entity;

import java.time.temporal.ChronoUnit;

public record RateLimitConfigEntity(Integer maxCount,
                                    Integer rate,
                                    ChronoUnit unit) {
}
