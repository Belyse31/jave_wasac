package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.entity.Notification;
import com.wasac.utilitybilling.entity.enums.NotificationType;
import com.wasac.utilitybilling.repository.NotificationRepository;
import com.wasac.utilitybilling.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;

    @Override
    public void create(String email, String phone, NotificationType type, String subject, String message, boolean sent) {
        Notification notification = new Notification();
        notification.setRecipientEmail(email);
        notification.setRecipientPhone(phone);
        notification.setType(type);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setSent(sent);
        repository.save(notification);
    }
}
