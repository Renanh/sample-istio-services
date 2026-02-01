package com.github.renanh.caller.infrastructure.config;

import com.github.renanh.caller.infrastructure.client.CallmeServiceClient;
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

    private final CallmeClientProperties callmeClientProperties;

    @Bean
    public RestClient callmeRestClient() {
        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofMillis(callmeClientProperties.getTimeout()));

        return RestClient.builder()
                .baseUrl(callmeClientProperties.getUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public CallmeServiceClient callmeServiceClient(RestClient callmeRestClient) {
        var adapter = RestClientAdapter.create(callmeRestClient);
        var factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(CallmeServiceClient.class);
    }
}
