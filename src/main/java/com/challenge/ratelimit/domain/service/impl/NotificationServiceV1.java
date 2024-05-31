package com.challenge.ratelimit.domain.service.impl;

import com.challenge.ratelimit.domain.exception.NotificationRejectedException;
import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.domain.model.RateLimitConfig;
import com.challenge.ratelimit.domain.port.NotificationDispatcher;
import com.challenge.ratelimit.domain.port.NotificationRepository;
import com.challenge.ratelimit.domain.port.RateLimitConfiguration;
import com.challenge.ratelimit.domain.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceV1 implements NotificationService {

    private final RateLimitConfiguration<NotificationType> rateLimitConfig;
    private final NotificationDispatcher notificationDispatcher;
    private final NotificationRepository notificationRepository;

    @Override
    public Mono<Notification> sendNotification(final Notification notification) {
        Mono<RateLimitConfig> rateLimitConfigMono = getRateLimitConfig(notification.type())
                .cache();
        return Mono.just(notification)
                .doOnNext(notif -> log.info("Received {} notification to send to user {}", notif.type(), notif.userId()))
                .filterWhen(notif -> this.isAllowed(notif, rateLimitConfigMono))
                .doOnDiscard(Notification.class,
                        notif -> log.info("Rejected {} notification for user {}", notif.type(), notif.userId()))
                .switchIfEmpty(Mono.error(() -> new NotificationRejectedException(notification.userId(), notification.type())))
                .flatMap(notificationDispatcher::dispatchNotification)
                .doOnNext(notif -> log.info("Sent {} notification to user {}", notif.type(), notif.userId()))
                .flatMap(notif -> notificationRepository.save(notif, rateLimitConfigMono));
    }

    private Mono<Boolean> isAllowed(final Notification notification, final Mono<RateLimitConfig> rateLimitConfigMono) {
        return notificationRepository.countByUserIdAndType(notification.userId(), notification.type())
                .zipWith(rateLimitConfigMono)
                .filter(tuple -> tuple.getT1() < tuple.getT2().maxCount())
                .hasElement()
                .doOnError(err -> log.error("Encountered error while checking notification count: {}", err.getMessage()))
                // TODO: define what to do on error (allow, reject, retry, etc.)
                .onErrorResume(e -> Mono.just(true));
    }

    private Mono<RateLimitConfig> getRateLimitConfig(final NotificationType type) {
        return rateLimitConfig.getRateLimitConfig(type)
                .defaultIfEmpty(new RateLimitConfig(1, Duration.ofMinutes(1)));
    }

}
