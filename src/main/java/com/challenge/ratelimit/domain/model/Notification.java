package com.challenge.ratelimit.domain.model;

import java.util.UUID;

public record Notification(UUID id,
                           UUID userId,
                           NotificationType type,
                           String content) {
}
