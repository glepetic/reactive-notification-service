package com.challenge.ratelimit.adapter;

import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationRequest;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResult;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResultResponse;
import com.challenge.ratelimit.infrastructure.dto.redis.NotificationEntry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class NotificationAdapter {

    public Notification toModel(final NotificationRequest request) {
        return new Notification(UUID.randomUUID(),
                UUID.fromString(request.userId()),
                NotificationType.valueOf(request.type()),
                request.content()
        );
    }

    public NotificationEntry buildEntry(final Notification notification,
                                        final Duration expiration) {
        return NotificationEntry.builder()
                .id(notification.id())
                .userId(notification.userId())
                .notificationType(notification.type())
                .content(notification.content())
                .expiration(expiration)
                .build();
    }

    public NotificationResultResponse toResultResponse(final Notification notification) {
        return new NotificationResultResponse(notification.id(), NotificationResult.OK);
    }

}
