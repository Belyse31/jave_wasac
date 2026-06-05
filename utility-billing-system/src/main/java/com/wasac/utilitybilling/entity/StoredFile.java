package com.wasac.utilitybilling.entity;

import com.wasac.utilitybilling.entity.enums.FileCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stored_files")
public class StoredFile extends BaseEntity {
    @Column(nullable = false)
    private String originalFilename;
    @Column(nullable = false)
    private String storedFilename;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false)
    private long sizeBytes;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory category;
    private String ownerReference;
}
