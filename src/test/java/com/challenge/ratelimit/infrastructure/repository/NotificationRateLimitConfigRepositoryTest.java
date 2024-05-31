package com.challenge.ratelimit.infrastructure.repository;

import com.challenge.ratelimit.adapter.RateLimitAdapter;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.domain.model.RateLimitConfig;
import com.challenge.ratelimit.infrastructure.dto.entity.RateLimitConfigEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationRateLimitConfigRepositoryTest {

    @InjectMocks
    private NotificationRateLimitConfigRepository repository;

    @Mock
    private RateLimitAdapter rateLimitAdapterMock;

    @Test
    @DisplayName("TEST getRateLimitConfig WHEN the configuration exists for received type THEN return config")
    public void getRateLimitConfigTest1() {

        // Assemble
        int maxCount = 1;
        int rate = 1;
        RateLimitConfigEntity entity = new RateLimitConfigEntity(maxCount, rate, ChronoUnit.DAYS);
        RateLimitConfig expectedConfig = new RateLimitConfig(maxCount, Duration.ofDays(rate));
        when(rateLimitAdapterMock.toModel(entity))
                .thenReturn(expectedConfig);

        // Act
        Mono<RateLimitConfig> rateLimitConfigMono = repository.getRateLimitConfig(NotificationType.NEWS);

        // Assert
        StepVerifier.create(rateLimitConfigMono)
                .expectNext(expectedConfig)
                .verifyComplete();

    }

    @Test
    @DisplayName("TEST getRateLimitConfig WHEN type param is null for received type THEN return empty")
    public void getRateLimitConfigTest2() {

        // Act
        Mono<RateLimitConfig> rateLimitConfigMono = repository.getRateLimitConfig(null);

        // Assert
        StepVerifier.create(rateLimitConfigMono)
                .verifyComplete();

    }

}
