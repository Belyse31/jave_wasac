package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
}
