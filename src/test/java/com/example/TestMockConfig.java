package com.example;

import com.example.kafka.UserEventProducer;
import com.example.service.CircuitBreakerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@TestConfiguration
public class TestMockConfig {

    @Bean
    @Primary
    public UserEventProducer userEventProducer() {
        return Mockito.mock(UserEventProducer.class);
    }

    @Bean
    @Primary
    public CircuitBreakerService circuitBreakerService() {
        return Mockito.mock(CircuitBreakerService.class);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
