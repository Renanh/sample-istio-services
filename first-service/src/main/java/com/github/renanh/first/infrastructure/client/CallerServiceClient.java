package com.github.renanh.first.infrastructure.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/caller")
public interface CallerServiceClient {

    @GetExchange("/ping")
    String ping();

    @GetExchange("/ping-with-random-error")
    String pingWithRandomError();

    @GetExchange("/ping-with-random-delay")
    String pingWithRandomDelay();
}
