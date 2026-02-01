package com.github.renanh.caller;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit5.HoverflyExtension;
import io.specto.hoverfly.junit5.api.HoverflyConfig;
import io.specto.hoverfly.junit5.api.HoverflyCore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(HoverflyExtension.class)
@HoverflyCore(config = @HoverflyConfig(proxyLocalHost = true))
@TestPropertySource(properties = {
        "services.callme.url=http://callme-service:8080"
})
@DisplayName("Caller Service - Integração com Callme Service")
class CallerCallmeTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Deve chamar callme-service e retornar resposta concatenada")
    void shouldCallCallmeService(Hoverfly hoverfly) {
        hoverfly.simulate(dsl(
                service("callme-service:8080")
                        .get("/callme/ping")
                        .willReturn(success("callme-service(v1)", "text/plain"))
        ));

        ResponseEntity<String> response = restClient.get()
                .uri("/caller/ping")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).contains("caller-service");
        assertThat(response.getBody()).contains("callme-service");
    }

    @Test
    @DisplayName("Deve chamar callme-service endpoint com erro aleatório")
    void shouldCallCallmeServiceWithRandomError(Hoverfly hoverfly) {
        hoverfly.simulate(dsl(
                service("callme-service:8080")
                        .get("/callme/ping-with-random-error")
                        .willReturn(success("callme-service(v1)", "text/plain"))
        ));

        ResponseEntity<String> response = restClient.get()
                .uri("/caller/ping-with-random-error")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).contains("caller-service");
    }

    @Test
    @DisplayName("Deve chamar callme-service endpoint com delay aleatório")
    void shouldCallCallmeServiceWithRandomDelay(Hoverfly hoverfly) {
        hoverfly.simulate(dsl(
                service("callme-service:8080")
                        .get("/callme/ping-with-random-delay")
                        .willReturn(success("callme-service(v1)", "text/plain"))
        ));

        ResponseEntity<String> response = restClient.get()
                .uri("/caller/ping-with-random-delay")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(response.getBody()).contains("caller-service");
    }
}
