package com.email.backend.repository;

import com.email.backend.model.Email;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EmailRepository extends MongoRepository<Email, String> {
    Email findByMessageId(String messageId);

    List<Email> findByUserId(String userId);

    List<Email> findByUserIdAndLabel(String userId, String label);
}
