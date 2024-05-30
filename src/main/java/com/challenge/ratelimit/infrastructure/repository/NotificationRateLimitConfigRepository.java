package com.challenge.ratelimit.infrastructure.repository;

import com.challenge.ratelimit.adapter.RateLimitAdapter;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.domain.model.RateLimitConfig;
import com.challenge.ratelimit.domain.port.RateLimitConfiguration;
import com.challenge.ratelimit.infrastructure.dto.entity.RateLimitConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.temporal.ChronoUnit;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class NotificationRateLimitConfigRepository implements RateLimitConfiguration<NotificationType> {

    // TODO: define where the config would be stored (application.yml, database, another microservice)
    private final Map<NotificationType, RateLimitConfigEntity> configMap = Map.of(
            NotificationType.STATUS, new RateLimitConfigEntity(2, 1, ChronoUnit.MINUTES),
            NotificationType.NEWS, new RateLimitConfigEntity(1, 1, ChronoUnit.DAYS),
            NotificationType.MARKETING, new RateLimitConfigEntity(3, 1, ChronoUnit.HOURS)
    );

    private final RateLimitAdapter rateLimitAdapter;

    @Override
    public Mono<RateLimitConfig> getRateLimitConfig(final NotificationType type) {
        return Mono.just(type)
                .mapNotNull(configMap::get)
                .map(rateLimitAdapter::toModel);
    }
}
