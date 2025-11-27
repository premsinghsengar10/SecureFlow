package com.email.backend.controller;

import com.email.backend.model.Email;
import com.email.backend.repository.EmailRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailRepository emailRepository;

    public EmailController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @GetMapping
    public List<Email> getAllEmails(@RequestParam(required = false) String userId,
            @RequestParam(required = false) String label) {
        if (userId != null && label != null) {
            return emailRepository.findByUserIdAndLabel(userId, label);
        } else if (userId != null) {
            return emailRepository.findByUserId(userId);
        }
        return emailRepository.findAll();
    }

    @GetMapping("/{id}")
    public Email getEmail(@PathVariable String id) {
        return emailRepository.findById(id).orElse(null);
    }
}
