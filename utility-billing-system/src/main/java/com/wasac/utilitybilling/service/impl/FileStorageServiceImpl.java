package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.dto.FileDtos;
import com.wasac.utilitybilling.entity.StoredFile;
import com.wasac.utilitybilling.entity.enums.FileCategory;
import com.wasac.utilitybilling.exception.BusinessRuleException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.StoredFileRepository;
import com.wasac.utilitybilling.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");
    private final StoredFileRepository repository;
    @Value("${app.files.upload-dir}")
    private String uploadDir;
    @Value("${app.files.max-size-bytes}")
    private long maxSizeBytes;

    @Override
    public FileDtos.FileResponse upload(MultipartFile file, FileCategory category, String ownerReference) {
        if (file.isEmpty()) {
            throw new BusinessRuleException("File cannot be empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BusinessRuleException("File exceeds maximum allowed size");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessRuleException("Only PDF, PNG, and JPEG files are allowed");
        }
        try {
            Path root = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(root);
            String storedName = UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
            Files.copy(file.getInputStream(), root.resolve(storedName));
            StoredFile storedFile = new StoredFile();
            storedFile.setOriginalFilename(file.getOriginalFilename());
            storedFile.setStoredFilename(storedName);
            storedFile.setContentType(file.getContentType());
            storedFile.setSizeBytes(file.getSize());
            storedFile.setCategory(category);
            storedFile.setOwnerReference(ownerReference);
            return response(repository.save(storedFile));
        } catch (IOException ex) {
            throw new BusinessRuleException("Unable to store file");
        }
    }

    @Override
    public List<FileDtos.FileResponse> list() {
        // List metadata records so users can copy a real file ID for download or delete.
        return repository.findAll().stream().map(this::response).toList();
    }

    @Override
    public Resource download(UUID id) {
        StoredFile file = entity(id);
        try {
            Resource resource = new UrlResource(Path.of(uploadDir).toAbsolutePath().normalize().resolve(file.getStoredFilename()).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Stored file content not found");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Stored file content not found");
        }
    }

    @Override
    public FileDtos.FileResponse metadata(UUID id) {
        return response(entity(id));
    }

    @Override
    public void delete(UUID id) {
        StoredFile file = entity(id);
        try {
            Files.deleteIfExists(Path.of(uploadDir).toAbsolutePath().normalize().resolve(file.getStoredFilename()));
        } catch (IOException ignored) {
        }
        repository.delete(file);
    }

    private StoredFile entity(UUID id) {
        // A 404 here means the ID is not from an uploaded file metadata record.
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    private String sanitize(String filename) {
        String name = filename == null ? "file" : filename;
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private FileDtos.FileResponse response(StoredFile file) {
        return new FileDtos.FileResponse(file.getId(), file.getOriginalFilename(), file.getContentType(), file.getSizeBytes(), file.getCategory(), file.getOwnerReference());
    }
}
