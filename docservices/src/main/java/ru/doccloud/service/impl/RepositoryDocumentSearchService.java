package ru.doccloud.service.impl;

import org.jtransfo.JTransfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.document.model.Document;
import ru.doccloud.repository.DocumentRepository;
import ru.doccloud.service.DocumentSearchService;

import java.util.List;

/**
 * @author Andrey Kadnikov
 */
@Service
public class RepositoryDocumentSearchService implements DocumentSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDocumentSearchService.class);

    private DocumentRepository repository;

    private JTransfo transformer;

    @Autowired
    public RepositoryDocumentSearchService(DocumentRepository repository, JTransfo transformer) {
        this.repository = repository;
        this.transformer = transformer;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable) {
        LOGGER.debug("entering findBySearchTerm(searchTerm={}, pageSize= {}, pageNumber = {})",
                searchTerm,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );

        Page<Document> searchResults = repository.findBySearchTerm(searchTerm, pageable);

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findBySearchTerm(): found {}", dtos);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }
}
