package com.challenge.ratelimit.infrastructure.dto.http.inbound;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NotificationResultResponse(UUID id,
                                         NotificationResult result) {
}
