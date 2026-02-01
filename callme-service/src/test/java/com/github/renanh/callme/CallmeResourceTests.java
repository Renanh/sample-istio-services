package com.github.renanh.callme;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CallmeResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ping_shouldReturnOkWithServiceName() throws Exception {
        mockMvc.perform(get("/callme/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("callme-service")));
    }

    @Test
    void pingWithRandomDelay_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/callme/ping-with-random-delay"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("callme-service")));
    }

    @Test
    void actuatorHealth_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }

    @Test
    void actuatorHealthLiveness_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }

    @Test
    void actuatorHealthReadiness_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }
}
