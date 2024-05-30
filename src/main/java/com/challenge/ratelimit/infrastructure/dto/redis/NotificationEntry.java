package com.challenge.ratelimit.infrastructure.dto.redis;

import com.challenge.ratelimit.domain.model.NotificationType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Builder
public record NotificationEntry(@Id @Getter UUID id,
                                UUID userId,
                                NotificationType notificationType,
                                String content) {

}
