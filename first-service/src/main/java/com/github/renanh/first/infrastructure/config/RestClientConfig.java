package com.github.renanh.first.infrastructure.config;

import com.github.renanh.first.infrastructure.client.CallerServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final CallerClientProperties callerClientProperties;

    @Bean
    public RestClient callerRestClient() {
        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofMillis(callerClientProperties.getTimeout()));

        return RestClient.builder()
                .baseUrl(callerClientProperties.getUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public CallerServiceClient callerServiceClient(RestClient callerRestClient) {
        var adapter = RestClientAdapter.create(callerRestClient);
        var factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(CallerServiceClient.class);
    }
}
