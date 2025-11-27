package com.email.backend.controller;

import com.email.backend.service.EmailFetcherService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fetch")
public class FetchController {

    private final EmailFetcherService emailFetcherService;

    public FetchController(EmailFetcherService emailFetcherService) {
        this.emailFetcherService = emailFetcherService;
    }

    @PostMapping
    public String triggerFetch() {
        new Thread(emailFetcherService::fetchUnreadEmails).start();
        return "Fetch triggered successfully";
    }
}
