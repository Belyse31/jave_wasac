package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.entity.AuditLog;
import com.wasac.utilitybilling.repository.AuditLogRepository;
import com.wasac.utilitybilling.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    private final AuditLogRepository repository;

    @Override
    public void record(String username, String action, String ipAddress, String details) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setIpAddress(ipAddress);
        log.setDetails(details);
        repository.save(log);
    }
}
