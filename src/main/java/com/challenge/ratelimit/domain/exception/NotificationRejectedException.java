package com.challenge.ratelimit.domain.exception;

import com.challenge.ratelimit.domain.model.NotificationType;

import java.util.UUID;

public class NotificationRejectedException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "Cannot send more notifications of type %s to user %s at this time";

    public NotificationRejectedException(final UUID recipent, final NotificationType type) {
        super(String.format(MESSAGE_TEMPLATE, type, recipent));
    }

}
