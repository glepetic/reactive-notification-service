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
    public Mono<Long> countByRecipientAndType(final UUID userId, final NotificationType type) {
        return Mono.fromSupplier(() -> buildKey(userId, type, "*"))
                .flatMapMany(redisTemplate::keys)
                .count()
                .defaultIfEmpty(0L);
    }

    @Override
    public Mono<Notification> save(final Notification notification, final Mono<RateLimitConfig> rateLimitConfigMono) {
        return Mono.just(notification)
                .map(notificationAdapter::toEntry)
                .zipWith(rateLimitConfigMono)
                .flatMap(tuple -> redisTemplate.opsForValue()
                        .set(buildKey(tuple.getT1().userId(), tuple.getT1().notificationType(), tuple.getT1().id().toString()), tuple.getT1(),
                                tuple.getT2().rate()))
                .thenReturn(notification);
    }

    private String buildKey(final UUID userId, final NotificationType type, final String id) {
        return String.format(KEY_TEMPLATE, userId, type, id);
    }

}
