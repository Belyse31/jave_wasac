package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.FileDtos;
import com.wasac.utilitybilling.entity.enums.FileCategory;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileStorageService {
    FileDtos.FileResponse upload(MultipartFile file, FileCategory category, String ownerReference);
    List<FileDtos.FileResponse> list();
    Resource download(UUID id);
    FileDtos.FileResponse metadata(UUID id);
    void delete(UUID id);
}
