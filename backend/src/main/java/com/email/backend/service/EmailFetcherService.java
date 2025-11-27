package com.email.backend.service;

import com.email.backend.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Service
public class EmailFetcherService {

    @Value("${mail.imap.host}")
    private String imapHost;

    @Value("${mail.imap.port}")
    private String imapPort;

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public EmailFetcherService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void fetchUnreadEmails() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", imapHost);
        props.put("mail.imaps.port", imapPort);

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(imapHost, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Search for unread messages
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            System.out.println("Found " + messages.length + " unread emails.");

            for (Message message : messages) {
                if (message instanceof MimeMessage) {
                    processMessage((MimeMessage) message);
                    // Mark as seen
                    message.setFlag(Flags.Flag.SEEN, true);
                }
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(MimeMessage message) throws MessagingException, IOException {
        Map<String, Object> rawEmail = new HashMap<>();
        rawEmail.put("messageId", UUID.randomUUID().toString());
        rawEmail.put("userId", username); // Use email as userId for now
        rawEmail.put("subject", message.getSubject());
        rawEmail.put("from", InternetAddress.toString(message.getFrom()));
        rawEmail.put("receivedAt", message.getReceivedDate().toString());

        String body = getTextFromMessage(message);
        rawEmail.put("bodyText", body);

        try {
            String json = objectMapper.writeValueAsString(rawEmail);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.RAW_ROUTING_KEY, json);
            System.out.println("Fetched and queued email: " + message.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // prefer plain text
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }
}
