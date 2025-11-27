package com.email.backend.service;

import com.email.backend.config.RabbitMQConfig;
import com.email.backend.model.Email;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void sendNotification(String message) {
        try {
            Email email = objectMapper.readValue(message, Email.class);

            // Send WebSocket notification
            messagingTemplate.convertAndSend("/topic/notifications", email);

            System.out.println("WebSocket notification sent for: " + email.getMessageId());

        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
