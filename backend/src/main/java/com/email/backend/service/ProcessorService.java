package com.email.backend.service;

import com.email.backend.config.RabbitMQConfig;
import com.email.backend.model.Email;
import com.email.backend.repository.EmailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessorService {

    private final EmailRepository emailRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public ProcessorService(EmailRepository emailRepository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.emailRepository = emailRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.RAW_EMAILS_QUEUE)
    public void processRawEmail(String message) {
        try {
            Map<String, Object> rawEmail = objectMapper.readValue(message, Map.class);

            Email email = new Email();
            email.setMessageId((String) rawEmail.get("messageId"));
            email.setUserId((String) rawEmail.get("userId"));
            email.setSubject((String) rawEmail.get("subject"));
            email.setBodyText((String) rawEmail.get("bodyText"));
            email.setFrom((String) rawEmail.get("from"));
            email.setReceivedAt((String) rawEmail.get("receivedAt"));

            // Feature Extraction
            Map<String, Object> features = extractFeatures(email);
            email.setFeatures(features);

            // Save to MongoDB
            emailRepository.save(email);

            // Publish to processed-emails queue
            String json = objectMapper.writeValueAsString(email);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.PROCESSED_ROUTING_KEY, json);

            System.out.println("Processed email: " + email.getMessageId());

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> extractFeatures(Email email) {
        Map<String, Object> features = new HashMap<>();

        String subject = email.getSubject() != null ? email.getSubject() : "";
        String bodyText = email.getBodyText() != null ? email.getBodyText() : "";

        features.put("subject_len", subject.length());
        features.put("num_links", countLinks(bodyText));
        features.put("has_attachment", false); // Placeholder
        features.put("has_urgent_words", checkUrgency(subject, bodyText));
        features.put("sender_in_contacts", false); // Placeholder
        features.put("spf_result", null);
        features.put("dkim_result", null);
        features.put("body_text", bodyText);
        features.put("subject_text", subject);

        return features;
    }

    private int countLinks(String text) {
        if (text == null)
            return 0;
        return text.split("http").length - 1;
    }

    private boolean checkUrgency(String subject, String body) {
        String content = (subject + " " + body).toLowerCase();
        return content.contains("urgent") ||
                content.contains("asap") ||
                content.contains("action required") ||
                content.contains("immediate");
    }
}
