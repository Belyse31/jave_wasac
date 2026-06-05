package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.entity.enums.NotificationType;

public interface NotificationService {
    void create(String email, String phone, NotificationType type, String subject, String message, boolean sent);
}
