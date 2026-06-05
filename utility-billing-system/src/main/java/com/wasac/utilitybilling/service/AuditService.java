package com.wasac.utilitybilling.service;

public interface AuditService {
    void record(String username, String action, String ipAddress, String details);
}
