package com.challenge.ratelimit.infrastructure.dto.redis;

import com.challenge.ratelimit.domain.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationKey implements Serializable {
    UUID userId;
    NotificationType type;
}
