package com.challenge.ratelimit.infrastructure.publisher;

import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.port.NotificationDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class NotificationPublisher implements NotificationDispatcher {

    @Override
    public Mono<Notification> dispatchNotification(final Notification notification) {
        // TODO: Define implementation, i.e: Kafka
        return Mono.just(notification);
    }

}
