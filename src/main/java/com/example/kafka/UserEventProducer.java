package com.example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendUserEvent(UserEventType eventType, String email, Long userId, String userName) {
        try {
            UserEvent event = new UserEvent(
                    eventType,
                    email,
                    userId,
                    userName,
                    System.currentTimeMillis()
            );

            String eventJson = objectMapper.writeValueAsString(event);

            log.info("📤 Sending to Kafka: {}", eventJson);
            kafkaTemplate.send("user-events", email, eventJson);
            log.info("✅ Event sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send event: {}", e.getMessage());
        }
    }
}

