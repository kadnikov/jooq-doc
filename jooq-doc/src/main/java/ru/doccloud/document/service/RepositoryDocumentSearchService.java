package ru.doccloud.document.service;

import java.util.List;

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

import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.repository.DocumentRepository;

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
        LOGGER.info("Finding {} Document entries for page {} by using search term: {}",
                pageable.getPageSize(),
                pageable.getPageNumber(),
                searchTerm
        );

        Page<Document> searchResults = repository.findBySearchTerm(searchTerm, pageable);
        LOGGER.info("Found {} Document entries for page: {}",
                searchResults.getNumberOfElements(),
                searchResults.getNumber()
        );

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }
}
