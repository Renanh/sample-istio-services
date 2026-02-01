package com.github.renanh.callme.api.resource;

import com.github.renanh.callme.domain.service.CallmeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callme")
@Tag(name = "Callme Service", description = "Backend service with error simulation and JFR monitoring")
@RequiredArgsConstructor
public class CallmeResource {

    private final CallmeService callmeService;

    @GetMapping("/ping")
    @Operation(summary = "Simple ping",
            description = "Returns a simple response with service version")
    @ApiResponse(responseCode = "200", description = "Successful ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok(callmeService.ping());
    }

    @GetMapping("/ping-with-random-error")
    @Operation(summary = "Ping with random error",
            description = "50% chance of returning HTTP 504 Gateway Timeout")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response"),
            @ApiResponse(responseCode = "504", description = "Simulated gateway timeout")
    })
    public ResponseEntity<String> pingWithRandomError() {
        return ResponseEntity.ok(callmeService.pingWithRandomError());
    }

    @GetMapping("/ping-with-random-delay")
    @Operation(summary = "Ping with random delay",
            description = "Random delay between 0-3000ms, recorded via JFR")
    @ApiResponse(responseCode = "200", description = "Successful response after delay")
    public ResponseEntity<String> pingWithRandomDelay() {
        return ResponseEntity.ok(callmeService.pingWithRandomDelay());
    }
}
