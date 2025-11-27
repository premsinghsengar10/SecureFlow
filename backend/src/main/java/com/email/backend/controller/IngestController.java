package com.email.backend.controller;

import com.email.backend.config.RabbitMQConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class IngestController {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public IngestController(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/ingest")
    public Map<String, String> ingestEmail(@RequestBody Map<String, Object> emailData) throws JsonProcessingException {
        // Add metadata
        emailData.put("receivedAt", LocalDateTime.now().toString());
        if (!emailData.containsKey("messageId")) {
            emailData.put("messageId", UUID.randomUUID().toString());
        }
        if (!emailData.containsKey("userId")) {
            emailData.put("userId", "user123"); // Default for MVP
        }

        String json = objectMapper.writeValueAsString(emailData);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.RAW_ROUTING_KEY, json);

        return Map.of(
                "status", "success",
                "messageId", (String) emailData.get("messageId"),
                "message", "Email ingested successfully");
    }
}
