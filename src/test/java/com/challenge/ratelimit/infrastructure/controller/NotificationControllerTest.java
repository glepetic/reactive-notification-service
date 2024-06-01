package com.challenge.ratelimit.infrastructure.controller;

import com.challenge.ratelimit.adapter.NotificationAdapter;
import com.challenge.ratelimit.domain.exception.NotificationRejectedException;
import com.challenge.ratelimit.domain.model.Notification;
import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.domain.service.NotificationService;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.ErrorResponse;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationRequest;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResult;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResultResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ActiveProfiles(value = "test")
@ExtendWith(MockitoExtension.class)
@WebFluxTest(controllers = NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private NotificationService notificationServiceMock;
    @MockBean
    private NotificationAdapter notificationAdapterMock;

    @Test
    @DisplayName("TEST sendNotification WHEN body is correct THEN expect 202 Accepted")
    public void sendNotificationTest1() {

        // Assemble
        UUID userId = UUID.randomUUID();
        String content = "Hello there!";
        NotificationType type = NotificationType.STATUS;
        NotificationRequest request = new NotificationRequest(type.name(), userId.toString(), content);
        Notification notification = new Notification(UUID.randomUUID(), userId, type, content);
        NotificationResultResponse expected = new NotificationResultResponse(UUID.randomUUID(), NotificationResult.OK);
        when(notificationAdapterMock.toModel(request))
                .thenReturn(notification);
        when(notificationServiceMock.sendNotification(notification))
                .thenReturn(Mono.just(notification));
        when(notificationAdapterMock.toResultResponse(notification))
                .thenReturn(expected);

        // Act
        WebTestClient.ResponseSpec responseSpec = webTestClient.post()
                .uri("/v1/notification")
                .bodyValue(request)
                .exchange();

        // Assert
        responseSpec.expectStatus().isAccepted()
                .expectBody(NotificationResultResponse.class)
                .value(body -> assertEquals(expected, body));

    }

    @Test
    @DisplayName("TEST sendNotification WHEN service rejects notification THEN expect 429 Too Many Requests")
    public void sendNotificationTest2() {

        // Assemble
        UUID userId = UUID.randomUUID();
        String content = "Hello there!";
        NotificationType type = NotificationType.STATUS;
        NotificationRequest request = new NotificationRequest(type.name(), userId.toString(), content);
        Notification notification = new Notification(UUID.randomUUID(), userId, type, content);
        String expectedErrorMessage = String.format("Cannot send notifications of type %s to user %s at this time",
                type, userId);
        when(notificationAdapterMock.toModel(request))
                .thenReturn(notification);
        when(notificationServiceMock.sendNotification(notification))
                .thenReturn(Mono.error(new NotificationRejectedException(userId, type)));

        // Act
        WebTestClient.ResponseSpec responseSpec = webTestClient.post()
                .uri("/v1/notification")
                .bodyValue(request)
                .exchange();

        // Assert
        responseSpec.expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals("Limit Exceeded", body.message());
                    assertEquals(1, body.errors().size());
                    assertEquals(expectedErrorMessage, body.errors().getFirst());
                });

    }

    @Test
    @DisplayName("TEST sendNotification WHEN body has invalid values on attributes THEN expect 400 Bad request - validation failed")
    public void sendNotificationTest3() {

        // Assemble
        NotificationRequest request1 = new NotificationRequest(null, null, null);
        NotificationRequest request2 = new NotificationRequest("DOESNT_EXIST", "doesnt-follow-format", "");
        NotificationRequest request3 = new NotificationRequest("STATUS", "doesnt-follow-format", "Has content");
        NotificationRequest request4 = new NotificationRequest("NON_EXISTENT", UUID.randomUUID().toString(), "");
        String expectedErrorMessage = "Validation failed";

        // Act
        WebTestClient.ResponseSpec responseSpec1 = webTestClient.post()
                .uri("/v1/notification")
                .bodyValue(request1)
                .exchange();

        WebTestClient.ResponseSpec responseSpec2 = webTestClient.post()
                .uri("/v1/notification")
                .bodyValue(request2)
                .exchange();

        WebTestClient.ResponseSpec responseSpec3 = webTestClient.post()
                .uri("/v1/notification")
                .bodyValue(request3)
                .exchange();

        WebTestClient.ResponseSpec responseSpec4 = webTestClient.post()
                .uri("/v1/notification")
                .bodyValue(request4)
                .exchange();

        // Assert
        responseSpec1.expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertEquals(3, body.errors().size());
                });

        responseSpec2.expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertEquals(3, body.errors().size());
                });

        responseSpec3.expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertEquals(1, body.errors().size());
                });

        responseSpec4.expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertEquals(2, body.errors().size());
                });

    }

    @Test
    @DisplayName("TEST sendNotification WHEN body is not present or not a valid json THEN expect 400 Bad request - invalid input")
    public void sendNotificationTest4() {

        // Assemble
        String expectedErrorMessage = "Invalid input";

        // Act
        WebTestClient.ResponseSpec responseSpec1 = webTestClient.post()
                .uri("/v1/notification")
                .header("Content-Type", "application/json")
                .body(BodyInserters.fromValue("invalid json"))
                .exchange();

        WebTestClient.ResponseSpec responseSpec2 = webTestClient.post()
                .uri("/v1/notification")
                .exchange();

        // Assert
        responseSpec1.expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertEquals(1, body.errors().size());
                });

        responseSpec2.expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertEquals(1, body.errors().size());
                });

    }

    @Test
    @DisplayName("TEST sendNotification WHEN incorrect request seems fishy THEN expect 403 Forbidden")
    public void sendNotificationTest5() {

        // Assemble
        String expectedErrorMessage = "Forbidden";

        // Act
        WebTestClient.ResponseSpec responseSpec1 = webTestClient.get()
                .uri("/v1/notification")
                .exchange();

        WebTestClient.ResponseSpec responseSpec2 = webTestClient.post()
                .uri("/v1/notification")
                .header("Content-Type", "text/javascript")
                .exchange();

        WebTestClient.ResponseSpec responseSpec3 = webTestClient.post()
                .uri("/v1/notify")
                .exchange();

        // Assert
        responseSpec1.expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertNull(body.errors());
                });

        responseSpec2.expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertNull(body.errors());
                });

        responseSpec3.expectStatus().isForbidden()
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals(expectedErrorMessage, body.message());
                    assertNull(body.errors());
                });

    }

    @Test
    @DisplayName("TEST sendNotification WHEN unknown error occurs on service THEN expect 500 Internal Server Error")
    public void sendNotificationTest6() {

        // Assemble
        UUID userId = UUID.randomUUID();
        String content = "Hello there!";
        NotificationType type = NotificationType.STATUS;
        NotificationRequest request = new NotificationRequest(type.name(), userId.toString(), content);
        Notification notification = new Notification(UUID.randomUUID(), userId, type, content);
        String expectedErrorMessage = "oops, something bad happened";
        when(notificationAdapterMock.toModel(request))
                .thenReturn(notification);
        when(notificationServiceMock.sendNotification(notification))
                .thenReturn(Mono.error(new RuntimeException(expectedErrorMessage)));

        // Act
        WebTestClient.ResponseSpec responseSpec = webTestClient.post()
                .uri("/v1/notification")
                .bodyValue(request)
                .exchange();

        // Assert
        responseSpec.expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody(ErrorResponse.class)
                .value(body -> {
                    assertEquals("Unexpected error", body.message());
                    assertEquals(1, body.errors().size());
                    assertEquals(expectedErrorMessage, body.errors().getFirst());
                });

    }

}
