package com.github.renanh.caller.infrastructure.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/callme")
public interface CallmeServiceClient {

    @GetExchange("/ping")
    String ping();

    @GetExchange("/ping-with-random-error")
    String pingWithRandomError();

    @GetExchange("/ping-with-random-delay")
    String pingWithRandomDelay();
}
