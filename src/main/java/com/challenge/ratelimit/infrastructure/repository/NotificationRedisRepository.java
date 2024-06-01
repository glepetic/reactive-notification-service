package com.challenge.ratelimit.infrastructure.repository;

import com.challenge.ratelimit.adapter.NotificationAdapter;
import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.domain.model.RateLimitConfig;
import com.challenge.ratelimit.domain.port.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRedisRepository implements NotificationRepository {

    // notification:userId:type:id
    private static final String KEY_TEMPLATE = "notification:%s:%s:%s";

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final NotificationAdapter notificationAdapter;

    @Override
    public Mono<Long> countByUserIdAndType(final UUID userId, final NotificationType type) {
        return Mono.fromSupplier(() -> buildKey(userId, type, "*"))
                .flatMapMany(redisTemplate::keys)
                .count()
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Notification> save(final Notification notification, final Mono<RateLimitConfig> rateLimitConfigMono) {
        return rateLimitConfigMono
                .map(config -> notificationAdapter.buildEntry(notification, config.rate()))
                .flatMap(entry -> redisTemplate.opsForValue().set(
                        buildKey(entry.userId(), entry.notificationType(), entry.id().toString()),
                        entry,
                        entry.expiration())
                )
                .thenReturn(notification);
    }

    private String buildKey(final UUID userId, final NotificationType type, final String id) {
        return String.format(KEY_TEMPLATE, userId, type, id);
    }

}
