package com.challenge.ratelimit.infrastructure.dto.http.inbound;

import com.challenge.ratelimit.domain.model.NotificationType;
import com.challenge.ratelimit.infrastructure.validator.EnumValue;
import com.challenge.ratelimit.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record NotificationRequest(@EnumValue(enumClass = NotificationType.class) String type,
                                  @NotNull @Pattern(regexp = Constants.UUID_REGEX) String userId,
                                  @NotBlank String content) {
}
