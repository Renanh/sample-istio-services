package com.github.renanh.callme.domain.service;

import com.github.renanh.callme.domain.event.ProcessingEvent;
import com.github.renanh.callme.infrastructure.config.ServiceProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Service
@Getter
public class CallmeService {

    private static final SecureRandom random = new SecureRandom();

    private final ServiceProperties serviceProperties;
    private final String instanceId;

    public CallmeService(ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
        this.instanceId = UUID.randomUUID().toString().substring(0, 8);
    }

    public String ping() {
        log.info("Received ping request, version={}, instanceId={}", getVersion(), instanceId);
        return formatResponse();
    }

    public String pingWithRandomError() {
        log.info("Received ping-with-random-error request, version={}, instanceId={}", getVersion(), instanceId);

        if (random.nextBoolean()) {
            log.warn("Simulating gateway timeout error, instanceId={}", instanceId);
            throw new ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    String.format("Simulated timeout from callme-service(%s), instanceId=%s", getVersion(), instanceId)
            );
        }

        return formatResponse();
    }

    public String pingWithRandomDelay() {
        String eventId = UUID.randomUUID().toString();
        var processingEvent = new ProcessingEvent(eventId);
        processingEvent.begin();

        int delayMs = random.nextInt(3001); // 0-3000ms
        log.info("Received ping-with-random-delay request, version={}, instanceId={}, delayMs={}",
                getVersion(), instanceId, delayMs);

        try {
            Thread.sleep(delayMs);
            processingEvent.setProcessingTimeMs(delayMs);
            processingEvent.setStatus("SUCCESS");
            return formatResponse();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            processingEvent.setProcessingTimeMs(delayMs);
            processingEvent.setStatus("INTERRUPTED");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Processing was interrupted"
            );
        } finally {
            processingEvent.end();
            if (processingEvent.shouldCommit()) {
                processingEvent.commit();
            }
        }
    }

    public String getVersion() {
        return serviceProperties.getVersion();
    }

    private String formatResponse() {
        return String.format("callme-service(%s)", getVersion());
    }
}
