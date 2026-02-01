package com.github.renanh.caller;

import com.github.renanh.caller.domain.service.CallerService;
import com.github.renanh.caller.infrastructure.client.CallmeServiceClient;
import com.github.renanh.caller.infrastructure.config.ServiceProperties;
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
@DisplayName("Caller Service")
class CallerCallmeTest {

    @Mock
    private CallmeServiceClient callmeServiceClient;

    @Mock
    private ServiceProperties serviceProperties;

    private CallerService callerService;

    @BeforeEach
    void setUp() {
        when(serviceProperties.getVersion()).thenReturn("v1");
        callerService = new CallerService(callmeServiceClient, serviceProperties);
    }

    @Nested
    @DisplayName("ping()")
    class PingMethod {

        @Test
        @DisplayName("Deve chamar callme-service e retornar resposta formatada")
        void shouldCallCallmeService() {
            when(callmeServiceClient.ping()).thenReturn("callme-service(v1)");

            String result = callerService.ping();

            assertThat(result).isEqualTo("caller-service(v1) -> callme-service(v1)");
            verify(callmeServiceClient).ping();
        }
    }

    @Nested
    @DisplayName("pingWithRandomError()")
    class PingWithRandomErrorMethod {

        @Test
        @DisplayName("Deve chamar callme-service endpoint com erro aleatório")
        void shouldCallCallmeServiceWithRandomError() {
            when(callmeServiceClient.pingWithRandomError()).thenReturn("callme-service(v1)");

            String result = callerService.pingWithRandomError();

            assertThat(result).isEqualTo("caller-service(v1) -> callme-service(v1)");
            verify(callmeServiceClient).pingWithRandomError();
        }
    }

    @Nested
    @DisplayName("pingWithRandomDelay()")
    class PingWithRandomDelayMethod {

        @Test
        @DisplayName("Deve chamar callme-service endpoint com delay aleatório")
        void shouldCallCallmeServiceWithRandomDelay() {
            when(callmeServiceClient.pingWithRandomDelay()).thenReturn("callme-service(v1)");

            String result = callerService.pingWithRandomDelay();

            assertThat(result).isEqualTo("caller-service(v1) -> callme-service(v1)");
            verify(callmeServiceClient).pingWithRandomDelay();
        }
    }
}
