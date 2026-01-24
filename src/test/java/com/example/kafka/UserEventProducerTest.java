package com.example.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private UserEventProducer userEventProducer;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userEventProducer = new UserEventProducer(kafkaTemplate, objectMapper);
    }

    @Test
    void sendUserEvent_ShouldSendCorrectMessage() throws JsonProcessingException {
        String email = "test@example.com";
        Long userId = 123L;
        String userName = "Test User";
        UserEventType eventType = UserEventType.USER_CREATED;

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        userEventProducer.sendUserEvent(eventType, email, userId, userName);

        verify(kafkaTemplate).send(eq("user-events"), eq(email), messageCaptor.capture());

        String sentMessage = messageCaptor.getValue();

        UserEvent sentEvent = objectMapper.readValue(sentMessage, UserEvent.class);

        assertThat(sentEvent.getEventType()).isEqualTo(eventType);
        assertThat(sentEvent.getEmail()).isEqualTo(email);
        assertThat(sentEvent.getUserId()).isEqualTo(userId);
        assertThat(sentEvent.getUserName()).isEqualTo(userName);
        assertThat(sentEvent.getTimestamp()).isGreaterThan(0);
    }
}