package ru.doccloud.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ru.doccloud.service.document.dto.DocumentDTO;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentSearchService {

    public Page<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable);
}
