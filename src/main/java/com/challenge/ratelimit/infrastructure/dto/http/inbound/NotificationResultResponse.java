package com.challenge.ratelimit.infrastructure.dto.http.inbound;

import java.util.UUID;

public record NotificationResultResponse(UUID id,
                                         NotificationResult result) {
}
