package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.FileDtos;
import com.wasac.utilitybilling.entity.enums.FileCategory;
import com.wasac.utilitybilling.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, ROLE_CUSTOMER: upload profile, customer, utility, or bill document")
    ApiResponse<FileDtos.FileResponse> upload(@RequestParam MultipartFile file, @RequestParam FileCategory category, @RequestParam(required = false) String ownerReference) {
        return ApiResponse.ok("File uploaded", service.upload(file, category, ownerReference));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, ROLE_CUSTOMER: list uploaded file metadata")
    ApiResponse<List<FileDtos.FileResponse>> list() {
        return ApiResponse.ok("Files retrieved", service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, ROLE_CUSTOMER: download file by real uploaded file ID")
    ResponseEntity<Resource> download(@Parameter(description = "Use the id returned by POST /api/files or GET /api/files", example = "11111111-1111-1111-1111-111111111111") @PathVariable UUID id) {
        FileDtos.FileResponse metadata = service.metadata(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.originalFilename() + "\"")
                .body(service.download(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    @Operation(summary = "ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, ROLE_CUSTOMER: delete file by real uploaded file ID")
    ApiResponse<Void> delete(@Parameter(description = "Use the id returned by POST /api/files or GET /api/files. Swagger sample UUIDs are not real records.", example = "11111111-1111-1111-1111-111111111111") @PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.ok("File deleted", null);
    }
}
