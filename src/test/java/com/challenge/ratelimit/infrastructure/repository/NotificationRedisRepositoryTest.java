package com.challenge.ratelimit.infrastructure.repository;

import com.challenge.ratelimit.adapter.NotificationAdapter;
import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.domain.model.RateLimitConfig;
import com.challenge.ratelimit.infrastructure.dto.redis.NotificationEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class NotificationRedisRepositoryTest {

    @InjectMocks
    private NotificationRedisRepository notificationRedisRepository;

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplateMock;
    @Mock
    private NotificationAdapter notificationAdapterMock;
    @Mock
    private ReactiveValueOperations<String, Object> reactiveValueOperationsMock;

    @Test
    @DisplayName("TEST countByUserIdAndType WHEN no notifications found THEN expect count 0")
    public void countByUserIdAndTypeTest1() {

        // Assemble
        UUID userId = UUID.randomUUID();
        NotificationType type = NotificationType.NEWS;
        String keys = "notification:" + userId + ":" + type + ":" + "*";

        when(redisTemplateMock.keys(keys))
                .thenReturn(Flux.empty());

        // Act
        Mono<Long> result = notificationRedisRepository.countByUserIdAndType(userId, type);

        // Assert
        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();

    }

    @Test
    @DisplayName("TEST countByUserIdAndType WHEN N notifications found THEN expect count N")
    public void countByUserIdAndTypeTest2() {

        // Assemble
        UUID userId = UUID.randomUUID();
        NotificationType type = NotificationType.NEWS;
        String keys = "notification:" + userId + ":" + type + ":" + "*";

        List<String> notificationsFound = List.of(
                "notification:" + userId + type + UUID.randomUUID(),
                "notification:" + userId + type + UUID.randomUUID(),
                "notification:" + userId + type + UUID.randomUUID(),
                "notification:" + userId + type + UUID.randomUUID(),
                "notification:" + userId + type + UUID.randomUUID()
        );
        Flux<String> notificationsFoundFlux = Flux.fromIterable(notificationsFound);
        long expectedCount = notificationsFound.size();

        when(redisTemplateMock.keys(keys))
                .thenReturn(notificationsFoundFlux);

        // Act
        Mono<Long> result = notificationRedisRepository.countByUserIdAndType(userId, type);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

    }

    @Test
    @DisplayName("TEST save WHEN all params are correct THEN expect notification saved and returned ok")
    public void saveTest1() {

        // Assemble
        UUID notificationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        NotificationType type = NotificationType.NEWS;
        String content = "content";
        Notification notification = new Notification(notificationId, userId, type, content);
        String entryKey = "notification:" + userId + ":" + type + ":" + notificationId;
        NotificationEntry entry = new NotificationEntry(notificationId, userId, type, content);
        Mono<RateLimitConfig> configMono = Mono.just(new RateLimitConfig(1, Duration.ofSeconds(10)));

        when(notificationAdapterMock.toEntry(notification))
                .thenReturn(entry);
        when(redisTemplateMock.opsForValue())
                .thenReturn(reactiveValueOperationsMock);
        when(reactiveValueOperationsMock.set(eq(entryKey), eq(entry), any(Duration.class)))
                .thenReturn(Mono.just(true));


        // Act
        Mono<Notification> result = notificationRedisRepository.save(notification, configMono);

        // Assert
        StepVerifier.create(result)
                .expectNext(notification)
                .verifyComplete();

    }

}
