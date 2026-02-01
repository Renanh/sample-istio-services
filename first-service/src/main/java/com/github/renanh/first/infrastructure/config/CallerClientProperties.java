package com.github.renanh.first.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services.caller")
@Getter
@Setter
public class CallerClientProperties {

    private String url = "http://caller-service:8080";
    private int timeout = 5000;
}
