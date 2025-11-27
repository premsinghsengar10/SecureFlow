package com.email.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_EXCHANGE = "email-exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification-exchange";

    public static final String RAW_EMAILS_QUEUE = "raw-emails";
    public static final String PROCESSED_EMAILS_QUEUE = "processed-emails";
    public static final String NOTIFICATION_QUEUE = "notifications";

    public static final String RAW_ROUTING_KEY = "email.raw";
    public static final String PROCESSED_ROUTING_KEY = "email.processed";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.important";

    @Bean
    TopicExchange emailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE);
    }

    @Bean
    TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    Queue rawEmailsQueue() {
        return new Queue(RAW_EMAILS_QUEUE, false);
    }

    @Bean
    Queue processedEmailsQueue() {
        return new Queue(PROCESSED_EMAILS_QUEUE, false);
    }

    @Bean
    Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, false);
    }

    @Bean
    Binding rawEmailsBinding(Queue rawEmailsQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(rawEmailsQueue).to(emailExchange).with(RAW_ROUTING_KEY);
    }

    @Bean
    Binding processedEmailsBinding(Queue processedEmailsQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(processedEmailsQueue).to(emailExchange).with(PROCESSED_ROUTING_KEY);
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(NOTIFICATION_ROUTING_KEY);
    }
}
