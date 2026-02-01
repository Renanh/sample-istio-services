package com.github.renanh.first;

import com.github.renanh.first.domain.service.FirstService;
import com.github.renanh.first.infrastructure.client.CallerServiceClient;
import com.github.renanh.first.infrastructure.config.ServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("First Service")
class FirstServiceTests {

    @Mock
    private CallerServiceClient callerServiceClient;

    @Mock
    private ServiceProperties serviceProperties;

    private FirstService firstService;

    @BeforeEach
    void setUp() {
        when(serviceProperties.getVersion()).thenReturn("v1");
        firstService = new FirstService(callerServiceClient, serviceProperties);
    }

    @Nested
    @DisplayName("ping()")
    class PingMethod {

        @Test
        @DisplayName("Deve retornar resposta formatada com cadeia de serviços")
        void shouldReturnFormattedResponse() {
            when(callerServiceClient.ping())
                    .thenReturn("caller-service(v1) -> callme-service(v1)");

            String result = firstService.ping();

            assertThat(result).isEqualTo("first-service(v1) -> caller-service(v1) -> callme-service(v1)");
            verify(callerServiceClient).ping();
        }
    }

    @Nested
    @DisplayName("pingWithRandomError()")
    class PingWithRandomErrorMethod {

        @Test
        @DisplayName("Deve retornar resposta formatada via endpoint com erro aleatório")
        void shouldReturnFormattedResponse() {
            when(callerServiceClient.pingWithRandomError())
                    .thenReturn("caller-service(v1) -> callme-service(v1)");

            String result = firstService.pingWithRandomError();

            assertThat(result).isEqualTo("first-service(v1) -> caller-service(v1) -> callme-service(v1)");
            verify(callerServiceClient).pingWithRandomError();
        }
    }

    @Nested
    @DisplayName("pingWithRandomDelay()")
    class PingWithRandomDelayMethod {

        @Test
        @DisplayName("Deve retornar resposta formatada via endpoint com delay aleatório")
        void shouldReturnFormattedResponse() {
            when(callerServiceClient.pingWithRandomDelay())
                    .thenReturn("caller-service(v1) -> callme-service(v1)");

            String result = firstService.pingWithRandomDelay();

            assertThat(result).isEqualTo("first-service(v1) -> caller-service(v1) -> callme-service(v1)");
            verify(callerServiceClient).pingWithRandomDelay();
        }
    }
}
