package ru.doccloud.document.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;
import ru.doccloud.document.repository.DocumentRepository;

/**
 * @author Andrey Kadnikov
 */
@Service
public class RepositoryDocumentCrudService implements DocumentCrudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDocumentCrudService.class);

    private final DocumentRepository repository;

    private final JTransfo transformer;

    @Autowired
    public RepositoryDocumentCrudService(DocumentRepository repository, JTransfo transformer) {
    	LOGGER.info("Create RepositoryDocumentCrudService");
        this.repository = repository;
        this.transformer = transformer;
    }
    
    public DocumentRepository getRepository(){
    	return this.repository;
    }

    @Transactional
    @Override
    public DocumentDTO add(DocumentDTO dto) {
        LOGGER.info("Adding Document entry with information: {}", dto);
        if (dto.getId()==null){
        	//dto.setId(DEFAULT);
        }
                
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        dto.setAuthor(request.getRemoteUser());
        Document added = createModel(dto);
        Document persisted = repository.add(added);

        LOGGER.info("Added Document entry with information: {}", persisted);

        return transformer.convert(persisted, new DocumentDTO());
    }
    
    @Transactional
    @Override
    public DocumentDTO addToFolder(DocumentDTO dto, Long folderId) {
        LOGGER.info("Adding Document entry with information: {}", dto);
        if (dto.getId()==null){
        	//dto.setId(DEFAULT);
        }
        Document added = createModel(dto);
        Document persisted = repository.add(added);
        
        Link parentLink = repository.addLink(folderId, persisted.getId());

        LOGGER.info("Added Document entry with information: {}", persisted);

        return transformer.convert(persisted, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO delete(Long id) {
        LOGGER.info("Deleting Document entry with id: {}", id);

        Document deleted = repository.delete(id);

        LOGGER.info("Deleted Document entry with id: {}", id);

        return transformer.convert(deleted, new DocumentDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public List<DocumentDTO> findAll() {
        LOGGER.info("Finding all Document entries.");
        
        List<Document> docEntries = repository.findAll();

        LOGGER.debug("Found {} Document entries.", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DocumentDTO> findAll(Pageable pageable) {
        LOGGER.info("Finding all Document entries.", 
                pageable.getPageSize(),
                pageable.getPageNumber());
        
        Page<Document> searchResults = repository.findAll(pageable);

        LOGGER.debug("Found {} Document entries.", searchResults.getNumber());

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public DocumentDTO findById(Long id) {
        LOGGER.info("Finding Document entry with id: {}", id);
        
        Document found = repository.findById(id);

        LOGGER.info("Found Document entry: {}", found);

        return transformer.convert(found, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO update(DocumentDTO dto) {
        LOGGER.info("Updating the information of a Document entry: {}", dto);
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        dto.setModifier(request.getRemoteUser());
        Document newInformation = createModel(dto);
        Document updated = repository.update(newInformation);

        LOGGER.debug("Updated the information of a Document entry: {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }

    private Document createModel(DocumentDTO dto) {
        return Document.getBuilder(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .data(dto.getData())
                .id(dto.getId())
                .author(dto.getAuthor())
                .modifier(dto.getModifier())
                .build();
    }

	@Override
	public Page<DocumentDTO> findAllByType(String type, String[] fields, Pageable pageable, String query) {
		LOGGER.info("Finding Documents by Type.");
		
        Page<Document> searchResults = repository.findAllByType(type, fields, pageable, query);

        LOGGER.debug("Found {} Document entries.", searchResults.getNumber());

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);
        
        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
	}

	@Override
	public List<DocumentDTO> findAllByParent(Long parentid) {
		LOGGER.info("Finding Documents by Type.");
		
        List<Document> docEntries = repository.findAllByParent(parentid);

        LOGGER.debug("Found {} Document entries.", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
	}
}
