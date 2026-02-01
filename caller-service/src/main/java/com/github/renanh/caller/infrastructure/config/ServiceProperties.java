package com.github.renanh.caller.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "service")
@Getter
@Setter
public class ServiceProperties {

    private String version = "v1";
}
