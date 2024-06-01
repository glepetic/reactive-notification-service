package com.challenge.ratelimit.infrastructure.controller;

import com.challenge.ratelimit.adapter.NotificationAdapter;
import com.challenge.ratelimit.domain.service.NotificationService;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationRequest;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.NotificationResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationAdapter notificationAdapter;

    // TODO: have all status codes documented for the EP (currently using automatic swagger generation)

    @PostMapping
    public Mono<ResponseEntity<NotificationResultResponse>> sendNotification(
            @RequestBody @Valid final Mono<NotificationRequest> notificationRequestMono) {
        return notificationRequestMono
                .map(notificationAdapter::toModel)
                .flatMap(notificationService::sendNotification)
                .map(notificationAdapter::toResultResponse)
                .map(resultResponse -> ResponseEntity.accepted().body(resultResponse));
    }

}
