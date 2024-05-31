package com.challenge.ratelimit.domain.port;

import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.domain.model.RateLimitConfig;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface NotificationRepository {
    Mono<Long> countByUserIdAndType(final UUID userId, final NotificationType type);
    Mono<Notification> save(final Notification notification, final Mono<RateLimitConfig> rateLimitConfigMono);
}
