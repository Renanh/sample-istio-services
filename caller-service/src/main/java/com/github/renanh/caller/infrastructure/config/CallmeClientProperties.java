package com.github.renanh.caller.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.callme")
@Getter
@Setter
public class CallmeClientProperties {

    private String url = "http://callme-service:8080";
    private int timeout = 5000;
}
