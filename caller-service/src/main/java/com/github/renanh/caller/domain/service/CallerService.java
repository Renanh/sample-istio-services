package com.github.renanh.caller.domain.service;

import com.github.renanh.caller.infrastructure.client.CallmeServiceClient;
import com.github.renanh.caller.infrastructure.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallerService {

    private final CallmeServiceClient callmeServiceClient;
    private final ServiceProperties serviceProperties;

    public String ping() {
        log.info("Received ping request, version={}", serviceProperties.getVersion());
        String response = callmeServiceClient.ping();
        return formatResponse(response);
    }

    public String pingWithRandomError() {
        log.info("Received ping-with-random-error request, version={}", serviceProperties.getVersion());
        String response = callmeServiceClient.pingWithRandomError();
        return formatResponse(response);
    }

    public String pingWithRandomDelay() {
        log.info("Received ping-with-random-delay request, version={}", serviceProperties.getVersion());
        String response = callmeServiceClient.pingWithRandomDelay();
        return formatResponse(response);
    }

    private String formatResponse(String callmeResponse) {
        return String.format("caller-service(%s) -> %s", serviceProperties.getVersion(), callmeResponse);
    }
}
