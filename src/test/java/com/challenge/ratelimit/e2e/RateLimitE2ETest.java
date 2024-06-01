package com.challenge.ratelimit.e2e;

import com.challenge.ratelimit.infrastructure.dto.http.inbound.ErrorResponse;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationRequest;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResult;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResultResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles(value = "test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RateLimitE2ETest {

    @Autowired
    private WebClient webClient;

    @Test
    @DisplayName("TEST notification rate limiter E2E")
    public void testRateLimitE2E() {

        // Assemble
        NotificationRequest request = new NotificationRequest("NEWS", "1d058b3f-2918-4ad1-8ed1-92506d9bf337", "Hi there!");

        // Act
        Mono<NotificationResultResponse> firstResultResponse = webClient.post()
                .uri("/v1/notification")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(NotificationResultResponse.class)
                .cache();

        Mono<ErrorResponse> secondResultResponse = firstResultResponse
                .then(webClient.post()
                        .uri("/v1/notification")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(ErrorResponse.class)
                        .onErrorResume(WebClientResponseException.class,
                                err -> Mono.justOrEmpty(err.getResponseBodyAs(ErrorResponse.class)))
                );

        // Assert
        StepVerifier.create(firstResultResponse)
                .assertNext(response -> {
                    assertNotNull(response.id());
                    assertEquals(NotificationResult.OK, response.result());
                })
                .verifyComplete();

        StepVerifier.create(secondResultResponse)
                .assertNext(response -> {
                    assertEquals("Limit Exceeded", response.message());
                    assertEquals(1, response.errors().size());
                })
                .verifyComplete();

    }

}
