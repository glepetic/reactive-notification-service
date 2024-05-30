package com.challenge.ratelimit.infrastructure.dto.http.inbound;

import java.util.List;

public record ErrorResponse(String message,
                            List<String> errors) {
}
