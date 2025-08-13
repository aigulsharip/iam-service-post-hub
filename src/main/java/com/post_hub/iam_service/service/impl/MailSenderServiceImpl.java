package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.entity.EmailVerificationToken;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.enums.RegistrationStatus;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.repository.EmailVerificationTokenRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.service.MailSenderService;
import com.post_hub.iam_service.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MailSenderServiceImpl implements MailSenderService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${base.url}")
    private String baseUrl;

    @Override
    public EmailVerificationToken createToken(User user) {
        tokenRepository.deleteByUser(user);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setCreated(LocalDateTime.now());
        token.setExpires(LocalDateTime.now().plusMinutes(30));
        token.setToken(ApiUtils.generateUuidWithoutDash());

        return tokenRepository.save(token);
    }

    @Override
    public EmailVerificationToken validateToken(String tokenValue) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.EMAIL_VERIFICATION_TOKEN_NOT_FOUND.getMessage()));

        if (token.getExpires().isBefore(LocalDateTime.now())) {
            throw new NotFoundException(ApiErrorMessage.CONFIRMATION_LINK_EXPIRED.getMessage());
        }

        User user = token.getUser();
        user.setRegistrationStatus(RegistrationStatus.ACTIVE);
        userRepository.save(user);

        tokenRepository.delete(token);

        return token;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String subject = "Confirm your registration";
        String confirmationUrl = baseUrl + "/auth/confirm?token=" + token;
        String message = "To complete your registration, please click the link below:\n" + confirmationUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }
}
