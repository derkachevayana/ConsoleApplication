package com.example.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private UserEventProducer userEventProducer;

    @Test
    void sendUserEvent_ShouldSendCorrectMessage() {
        String email = "test@example.com";
        Long userId = 123L;
        String userName = "Test User";

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        userEventProducer.sendUserEvent(UserEventType.USER_CREATED, email, userId, userName);

        verify(kafkaTemplate).send(eq("user-events"), eq(email), messageCaptor.capture());

        String sentMessage = messageCaptor.getValue();
        assertThat(sentMessage)
                .contains("\"eventType\":\"USER_CREATED\"")
                .contains("\"email\":\"test@example.com\"")
                .contains("\"userId\":123")
                .contains("\"userName\":\"Test User\"")
                .contains("\"timestamp\":");
    }
}