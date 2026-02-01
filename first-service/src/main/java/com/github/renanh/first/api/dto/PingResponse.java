package com.github.renanh.first.api.dto;

import org.apache.commons.lang3.Validate;

import java.time.Instant;

public record PingResponse(
        String message,
        String version,
        Instant timestamp
) {
    public PingResponse {
        Validate.notBlank(message, "message must not be blank");
        Validate.notBlank(version, "version must not be blank");
    }

    public static PingResponse of(String message, String version) {
        return new PingResponse(message, version, Instant.now());
    }
}
