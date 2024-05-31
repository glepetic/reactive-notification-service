package com.challenge.ratelimit.adapter;

import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationRequest;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResult;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResultResponse;
import com.challenge.ratelimit.infrastructure.dto.redis.NotificationEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class NotificationAdapterTest {

    @InjectMocks
    private NotificationAdapter notificationAdapter;

    @Test
    @DisplayName("TEST toModel WHEN request with non-null values is received THEN return notification model")
    public void toModelTest1() {

        // Assemble
        UUID expectedUserId = UUID.randomUUID();
        String expectedContent = "Hi there!";

        // Act
        Notification notification =
                notificationAdapter.toModel(new NotificationRequest("NEWS", expectedUserId.toString(), expectedContent));

        // Assert
        assertNotNull(notification.id());
        assertEquals(expectedUserId, notification.userId());
        assertEquals(NotificationType.NEWS, notification.type());
        assertEquals(expectedContent, notification.content());

    }

    @Test
    @DisplayName("TEST toEntry WHEN model with non-null values is received THEN return notification entry")
    public void toEntryTest1() {

        // Assemble
        UUID expectedId = UUID.randomUUID();
        UUID expectedUserId = UUID.randomUUID();
        NotificationType expectedType = NotificationType.NEWS;
        String expectedContent = "Hi there!";

        // Act
        NotificationEntry notificationEntry =
                notificationAdapter.toEntry(new Notification(expectedId, expectedUserId, expectedType, expectedContent));

        // Assert
        assertEquals(expectedId, notificationEntry.id());
        assertEquals(expectedUserId, notificationEntry.userId());
        assertEquals(expectedType, notificationEntry.notificationType());
        assertEquals(expectedContent, notificationEntry.content());

    }

    @Test
    @DisplayName("TEST toResultResponse WHEN model with non-null values is received THEN return result response")
    public void toResultResponseTest1() {

        // Assemble
        UUID expectedId = UUID.randomUUID();

        // Act
        NotificationResultResponse notificationResultResponse = notificationAdapter.toResultResponse(
                new Notification(expectedId, UUID.randomUUID(), NotificationType.NEWS, "Hi there!")
        );

        // Assert
        assertEquals(expectedId, notificationResultResponse.id());
        assertEquals(NotificationResult.OK, notificationResultResponse.result());

    }

}
