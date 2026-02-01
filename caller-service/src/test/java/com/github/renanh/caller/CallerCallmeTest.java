package com.github.renanh.caller;

import com.github.renanh.caller.infrastructure.client.CallmeServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Caller Service - Integração com Callme Service")
class CallerCallmeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CallmeServiceClient callmeServiceClient;

    @Nested
    @DisplayName("GET /caller/ping")
    class PingEndpoint {

        @Test
        @DisplayName("Deve chamar callme-service e retornar resposta concatenada")
        void shouldCallCallmeService() throws Exception {
            when(callmeServiceClient.ping()).thenReturn("callme-service(v1)");

            mockMvc.perform(get("/caller/ping"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("caller-service")))
                    .andExpect(content().string(containsString("callme-service")));

            verify(callmeServiceClient).ping();
        }
    }

    @Nested
    @DisplayName("GET /caller/ping-with-random-error")
    class PingWithRandomErrorEndpoint {

        @Test
        @DisplayName("Deve chamar callme-service endpoint com erro aleatório")
        void shouldCallCallmeServiceWithRandomError() throws Exception {
            when(callmeServiceClient.pingWithRandomError()).thenReturn("callme-service(v1)");

            mockMvc.perform(get("/caller/ping-with-random-error"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("caller-service")));

            verify(callmeServiceClient).pingWithRandomError();
        }
    }

    @Nested
    @DisplayName("GET /caller/ping-with-random-delay")
    class PingWithRandomDelayEndpoint {

        @Test
        @DisplayName("Deve chamar callme-service endpoint com delay aleatório")
        void shouldCallCallmeServiceWithRandomDelay() throws Exception {
            when(callmeServiceClient.pingWithRandomDelay()).thenReturn("callme-service(v1)");

            mockMvc.perform(get("/caller/ping-with-random-delay"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("caller-service")));

            verify(callmeServiceClient).pingWithRandomDelay();
        }
    }

    @Nested
    @DisplayName("Actuator Health Endpoints")
    class ActuatorHealthEndpoints {

        @Test
        @DisplayName("GET /actuator/health deve retornar UP")
        void healthShouldReturnUp() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("UP")));
        }
    }
}
