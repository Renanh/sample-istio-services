package com.github.renanh.first.domain.service;

import com.github.renanh.first.infrastructure.client.CallerServiceClient;
import com.github.renanh.first.infrastructure.config.ServiceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstService {

    private final CallerServiceClient callerServiceClient;
    private final ServiceProperties serviceProperties;

    public String ping() {
        log.info("Received ping request, version={}", serviceProperties.getVersion());
        String response = callerServiceClient.ping();
        return formatResponse(response);
    }

    public String pingWithRandomError() {
        log.info("Received ping-with-random-error request, version={}", serviceProperties.getVersion());
        String response = callerServiceClient.pingWithRandomError();
        return formatResponse(response);
    }

    public String pingWithRandomDelay() {
        log.info("Received ping-with-random-delay request, version={}", serviceProperties.getVersion());
        String response = callerServiceClient.pingWithRandomDelay();
        return formatResponse(response);
    }

    private String formatResponse(String callerResponse) {
        return String.format("first-service(%s) -> %s", serviceProperties.getVersion(), callerResponse);
    }
}
