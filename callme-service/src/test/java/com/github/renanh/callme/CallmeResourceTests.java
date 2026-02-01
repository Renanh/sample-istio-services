package com.github.renanh.callme;

import com.github.renanh.callme.domain.service.CallmeService;
import com.github.renanh.callme.infrastructure.config.ServiceProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Callme Service")
class CallmeResourceTests {

    @Mock
    private ServiceProperties serviceProperties;

    @Nested
    @DisplayName("ping()")
    class PingMethod {

        @Test
        @DisplayName("Deve retornar resposta formatada com versao do servico")
        void shouldReturnFormattedResponse() {
            when(serviceProperties.getVersion()).thenReturn("v1");
            CallmeService callmeService = new CallmeService(serviceProperties);

            String result = callmeService.ping();

            assertThat(result).isEqualTo("callme-service(v1)");
        }
    }

    @Nested
    @DisplayName("pingWithRandomDelay()")
    class PingWithRandomDelayMethod {

        @Test
        @DisplayName("Deve retornar resposta apos delay aleatorio")
        void shouldReturnResponseAfterDelay() {
            when(serviceProperties.getVersion()).thenReturn("v1");
            CallmeService callmeService = new CallmeService(serviceProperties);

            String result = callmeService.pingWithRandomDelay();

            assertThat(result).isEqualTo("callme-service(v1)");
        }
    }

    @Nested
    @DisplayName("getVersion()")
    class GetVersionMethod {

        @Test
        @DisplayName("Deve retornar versao configurada")
        void shouldReturnConfiguredVersion() {
            when(serviceProperties.getVersion()).thenReturn("v1");
            CallmeService callmeService = new CallmeService(serviceProperties);

            String version = callmeService.getVersion();

            assertThat(version).isEqualTo("v1");
        }
    }

    @Nested
    @DisplayName("getInstanceId()")
    class GetInstanceIdMethod {

        @Test
        @DisplayName("Deve retornar ID de instancia unico")
        void shouldReturnUniqueInstanceId() {
            when(serviceProperties.getVersion()).thenReturn("v1");
            CallmeService callmeService = new CallmeService(serviceProperties);

            String instanceId = callmeService.getInstanceId();

            assertThat(instanceId).isNotNull();
            assertThat(instanceId).hasSize(8);
        }
    }
}
