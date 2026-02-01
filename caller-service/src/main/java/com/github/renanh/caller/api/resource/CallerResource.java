package com.github.renanh.caller.api.resource;

import com.github.renanh.caller.domain.service.CallerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/caller")
@Tag(name = "Caller Service", description = "Middle layer service that calls callme-service")
@RequiredArgsConstructor
public class CallerResource {

    private final CallerService callerService;

    @GetMapping("/ping")
    @Operation(summary = "Ping callme-service",
            description = "Calls callme-service and returns the response chain")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok(callerService.ping());
    }

    @GetMapping("/ping-with-random-error")
    @Operation(summary = "Ping with random error",
            description = "Calls callme-service endpoint that randomly returns errors")
    public ResponseEntity<String> pingWithRandomError() {
        return ResponseEntity.ok(callerService.pingWithRandomError());
    }

    @GetMapping("/ping-with-random-delay")
    @Operation(summary = "Ping with random delay",
            description = "Calls callme-service endpoint that has random processing delay")
    public ResponseEntity<String> pingWithRandomDelay() {
        return ResponseEntity.ok(callerService.pingWithRandomDelay());
    }
}
