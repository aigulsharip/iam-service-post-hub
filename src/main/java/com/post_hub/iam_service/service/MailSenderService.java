package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.entity.EmailVerificationToken;
import com.post_hub.iam_service.model.entity.User;

public interface MailSenderService {
    EmailVerificationToken createToken(User user);

    EmailVerificationToken validateToken(String token);

    void sendVerificationEmail(String to, String token);
}
