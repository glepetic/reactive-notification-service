package com.challenge.ratelimit.adapter;

import com.challenge.ratelimit.domain.model.RateLimitConfig;
import com.challenge.ratelimit.infrastructure.dto.entity.RateLimitConfigEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RateLimitAdapterTest {

    @InjectMocks
    private RateLimitAdapter rateLimitAdapter;

    @Test
    @DisplayName("TEST toModel WHEN entity with non-null values is received THEN return config model")
    public void toModelTest1() {

        // Assemble
        int maxCountExpected = 5;
        int expectedRateMinutes = 2;
        Duration expectedRate = Duration.ofMinutes(expectedRateMinutes);

        // Act
        RateLimitConfig config =
                rateLimitAdapter.toModel(new RateLimitConfigEntity(5, expectedRateMinutes, ChronoUnit.MINUTES));

        // Assert
        assertEquals(maxCountExpected, config.maxCount());
        assertEquals(expectedRate, config.rate());

    }


}
