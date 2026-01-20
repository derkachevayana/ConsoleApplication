package com.example.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserEvent(String eventType, String email, Long userId, String userName) {
        try {
            String eventJson = String.format(
                    "{\"eventType\":\"%s\",\"email\":\"%s\",\"userId\":%d,\"userName\":\"%s\",\"timestamp\":%d}",
                    eventType, email, userId, userName, System.currentTimeMillis()
            );

            log.info("📤 Sending to Kafka: {}", eventJson);
            kafkaTemplate.send("user-events", email, eventJson);
            log.info("✅ Event sent successfully");

        } catch (Exception e) {
            log.error("❌ Failed to send event: {}", e.getMessage());
        }
    }
}

