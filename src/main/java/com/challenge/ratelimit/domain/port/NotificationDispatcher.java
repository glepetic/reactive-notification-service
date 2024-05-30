package com.challenge.ratelimit.domain.port;

import com.challenge.ratelimit.domain.model.Notification;
import reactor.core.publisher.Mono;

public interface NotificationDispatcher {
    Mono<Notification> dispatchNotification(Notification notification);
}
