package com.challenge.ratelimit.domain.service;

import com.challenge.ratelimit.domain.model.Notification;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Notification> sendNotification(final Notification notification);
}
