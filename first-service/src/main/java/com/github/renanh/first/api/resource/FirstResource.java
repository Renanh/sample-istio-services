package com.github.renanh.first.api.resource;

import com.github.renanh.first.domain.service.FirstService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/first")
@Tag(name = "First Service", description = "Entry point service that initiates the service call chain")
@RequiredArgsConstructor
public class FirstResource {

    private final FirstService firstService;

    @GetMapping("/ping")
    @Operation(summary = "Ping through service chain",
            description = "Calls caller-service which in turn calls callme-service")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok(firstService.ping());
    }

    @GetMapping("/ping-with-random-error")
    @Operation(summary = "Ping with random error simulation",
            description = "Demonstrates retry and circuit breaker patterns")
    public ResponseEntity<String> pingWithRandomError() {
        return ResponseEntity.ok(firstService.pingWithRandomError());
    }

    @GetMapping("/ping-with-random-delay")
    @Operation(summary = "Ping with random delay",
            description = "Demonstrates timeout handling in service mesh")
    public ResponseEntity<String> pingWithRandomDelay() {
        return ResponseEntity.ok(firstService.pingWithRandomDelay());
    }
}
