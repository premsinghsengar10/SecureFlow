package com.email.backend.service;

import com.email.backend.config.RabbitMQConfig;
import com.email.backend.model.Email;
import com.email.backend.repository.EmailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OrchestratorService {

    private final EmailRepository emailRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public OrchestratorService(EmailRepository emailRepository, RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.emailRepository = emailRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.PROCESSED_EMAILS_QUEUE)
    public void orchestrateClassification(String message) {
        try {
            Email email = objectMapper.readValue(message, Email.class);

            // Call ML Service for prediction
            try {
                Map<String, Object> features = email.getFeatures();

                ResponseEntity<Map> response = restTemplate.postForEntity(
                        mlServiceUrl + "/predict",
                        features,
                        Map.class);

                if (response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    Map<String, Double> scores = (Map<String, Double>) body.get("scores");
                    List<String> explanations = (List<String>) body.get("explanations");

                    email.setMlScores(scores);
                    email.setExplanations(explanations);

                    // Determine final label
                    String label = determineLabel(scores);
                    email.setLabel(label);

                    // Update in MongoDB
                    emailRepository.save(email);

                    // If important, send notification
                    if ("important".equals(label)) {
                        String notificationJson = objectMapper.writeValueAsString(email);
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                                notificationJson);
                        System.out.println("Notification queued for: " + email.getMessageId());
                    }

                    System.out.println("Classified email " + email.getMessageId() + " as: " + label);
                }
            } catch (Exception e) {
                System.err.println("Error calling ML service: " + e.getMessage());
                // Fallback: use simple rule-based classification
                email.setLabel("other");
                emailRepository.save(email);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private String determineLabel(Map<String, Double> scores) {
        double importantScore = scores.getOrDefault("important", 0.0);
        double spamScore = scores.getOrDefault("spam", 0.0);
        double fraudScore = scores.getOrDefault("fraud", 0.0);

        // Simple threshold-based classification
        if (fraudScore > 0.6) {
            return "fraud";
        } else if (importantScore > 0.5) {
            return "important";
        } else if (spamScore > 0.5) {
            return "spam";
        } else {
            return "other";
        }
    }
}
