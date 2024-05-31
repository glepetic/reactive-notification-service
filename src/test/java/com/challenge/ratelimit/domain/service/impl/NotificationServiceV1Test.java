package com.challenge.ratelimit.domain.service.impl;

import com.challenge.ratelimit.domain.exception.NotificationRejectedException;
import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.domain.model.RateLimitConfig;
import com.challenge.ratelimit.domain.port.NotificationDispatcher;
import com.challenge.ratelimit.domain.port.NotificationRepository;
import com.challenge.ratelimit.domain.port.RateLimitConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceV1Test {

    @InjectMocks
    private NotificationServiceV1 notificationService;

    @Mock
    private RateLimitConfiguration<NotificationType> rateLimitConfigurationMock;
    @Mock
    private NotificationDispatcher notificationDispatcherMock;
    @Mock
    private NotificationRepository notificationRepositoryMock;

    @Test
    @DisplayName("TEST sendNotification WHEN rate limit is not exceeded THEN expect notification sent")
    public void sendNotificationTest1() {

        // Assemble
        UUID userId = UUID.randomUUID();
        NotificationType type = NotificationType.NEWS;
        Notification notification =
                new Notification(UUID.randomUUID(), userId, type, "Hello there!");
        Mono<RateLimitConfig> rateLimitConfigMono = Mono.just(new RateLimitConfig(5, Duration.ofMinutes(1)))
                .cache();

        when(rateLimitConfigurationMock.getRateLimitConfig(type))
                .thenReturn(rateLimitConfigMono);
        when(notificationRepositoryMock.countByUserIdAndType(userId, type))
                .thenReturn(Mono.just(4L));
        when(notificationDispatcherMock.dispatchNotification(notification))
                .thenReturn(Mono.just(notification));
        when(notificationRepositoryMock.save(eq(notification), any()))
                .thenReturn(Mono.just(notification));

        // Act
        Mono<Notification> result = notificationService.sendNotification(notification);

        // Assert
        StepVerifier.create(result)
                .expectNext(notification)
                .verifyComplete();

        verify(rateLimitConfigurationMock, times(1))
                .getRateLimitConfig(type);
        verify(notificationDispatcherMock, times(1))
                .dispatchNotification(any());
        verify(notificationRepositoryMock, times(1))
                .save(any(), any());

    }

    @Test
    @DisplayName("TEST sendNotification WHEN rate limit is exceeded THEN expect NotificationRejectedException")
    public void sendNotificationTest2() {

        // Assemble
        UUID userId = UUID.randomUUID();
        NotificationType type = NotificationType.NEWS;
        Notification notification =
                new Notification(UUID.randomUUID(), userId, type, "Hello there!");
        Mono<RateLimitConfig> rateLimitConfigMono = Mono.just(new RateLimitConfig(5, Duration.ofMinutes(1)))
                .cache();

        when(rateLimitConfigurationMock.getRateLimitConfig(type))
                .thenReturn(rateLimitConfigMono);
        when(notificationRepositoryMock.countByUserIdAndType(userId, type))
                .thenReturn(Mono.just(5L));

        // Act
        Mono<Notification> result = notificationService.sendNotification(notification);

        // Assert
        StepVerifier.create(result)
                .expectError(NotificationRejectedException.class)
                .verify();

        verify(rateLimitConfigurationMock, times(1))
                .getRateLimitConfig(type);
        verify(notificationDispatcherMock, never())
                .dispatchNotification(any());
        verify(notificationRepositoryMock, never())
                .save(any(), any());

    }

}
