package com.email.ingest;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
public class IngestController {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public IngestController(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/ingest")
    public String ingestEmail(@RequestBody Map<String, Object> emailData) throws JsonProcessingException {
        // Add metadata
        emailData.put("receivedAt", LocalDateTime.now().toString());
        if (!emailData.containsKey("messageId")) {
            emailData.put("messageId", UUID.randomUUID().toString());
        }

        String json = objectMapper.writeValueAsString(emailData);
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, json);

        return "Email ingested successfully: " + emailData.get("messageId");
    }
}
