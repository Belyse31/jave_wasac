package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.entity.enums.FileCategory;

import java.util.UUID;

public final class FileDtos {
    private FileDtos() {
    }

    public record FileResponse(UUID id, String originalFilename, String contentType, long sizeBytes, FileCategory category, String ownerReference) {
    }
}
