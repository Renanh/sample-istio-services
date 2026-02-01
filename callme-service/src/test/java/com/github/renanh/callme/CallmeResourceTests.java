package com.github.renanh.callme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Callme Resource")
class CallmeResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /callme/ping")
    class PingEndpoint {

        @Test
        @DisplayName("Deve retornar 200 OK com nome do serviço")
        void shouldReturnOkWithServiceName() throws Exception {
            mockMvc.perform(get("/callme/ping"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("callme-service")));
        }
    }

    @Nested
    @DisplayName("GET /callme/ping-with-random-delay")
    class PingWithRandomDelayEndpoint {

        @Test
        @DisplayName("Deve retornar 200 OK mesmo com delay")
        void shouldReturnOkWithDelay() throws Exception {
            mockMvc.perform(get("/callme/ping-with-random-delay"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("callme-service")));
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

        @Test
        @DisplayName("GET /actuator/health/liveness deve retornar UP")
        void livenessShouldReturnUp() throws Exception {
            mockMvc.perform(get("/actuator/health/liveness"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("UP")));
        }

        @Test
        @DisplayName("GET /actuator/health/readiness deve retornar UP")
        void readinessShouldReturnUp() throws Exception {
            mockMvc.perform(get("/actuator/health/readiness"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("UP")));
        }
    }
}
