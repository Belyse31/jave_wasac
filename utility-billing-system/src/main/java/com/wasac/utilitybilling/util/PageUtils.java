package com.wasac.utilitybilling.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {
    private PageUtils() {
    }

    public static Pageable pageable(int page, int size, String sortField, String sortDirection) {
        int normalizedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(Math.max(page, 0), normalizedSize, Sort.by(direction, sortField == null || sortField.isBlank() ? "createdAt" : sortField));
    }
}
