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

    @Override
    public DocumentDTO add(DocumentDTO todo) {
        return null;
    }

    @Transactional
    @Override
    public DocumentDTO add(final DocumentDTO dto, final String user) {
        LOGGER.info("Adding Document entry with information: {}", dto);
        if (dto.getId()==null){
            //dto.setId(DEFAULT);
        }

        repository.setUser(user);
        dto.setAuthor(user);
        Document persisted = repository.add(createModel(dto));

        LOGGER.info("Added Document entry with information: {}", persisted);

        return transformer.convert(persisted, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO addToFolder(final DocumentDTO dto, final Long folderId) {
        LOGGER.info("Adding Document entry with information: {}", dto);
        if (dto.getId()==null){
            //dto.setId(DEFAULT);
        }

        Document persisted = null;
//        try to find document in database
        if(dto.getId() != null) {
            persisted = repository.findById(dto.getId());
        }


        if(persisted == null)
            persisted = repository.add(createModel(dto));

        repository.addLink(folderId, persisted.getId());

        LOGGER.info("Added Document entry with information: {}", persisted);

        return transformer.convert(persisted, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO delete(final Long id) {
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
    public Page<DocumentDTO> findAll(final Pageable pageable) {
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

    @Override
    public List<DocumentDTO> findParents(Long docId){
        final List<Document> docEntries = repository.findParents(docId);

        if(docEntries == null) {
            LOGGER.info("There are no parents for document with ID {}", docId);
            return null;
        }

        return transformer.convertList(docEntries, DocumentDTO.class);
    }


    @Transactional(readOnly = true)
    @Override
    public List<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable){
        Page<Document> docPage = repository.findBySearchTerm(searchTerm, pageable);
        return  transformer.convertList(docPage.getContent(), DocumentDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public DocumentDTO findById(final Long id) {
        LOGGER.info("Finding Document entry with id: {}", id);

        Document found = repository.findById(id);

        LOGGER.info("Found Document entry: {}", found);

        return transformer.convert(found, new DocumentDTO());
    }

    @Override
    public DocumentDTO update(DocumentDTO updated) {
        return null;
    }

    @Transactional
    @Override
    public DocumentDTO update(final DocumentDTO dto, final String user) {
        LOGGER.info("Updating the information of a Document entry: {}", dto);

//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        dto.setModifier(user);
        Document newInformation = createModel(dto);
        Document updated = repository.update(newInformation);

        LOGGER.debug("Updated the information of a Document entry: {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO updateFileInfo(final DocumentDTO dto){
        final Document updated = repository.updateFileInfo(createModel(dto));

        LOGGER.debug("Updated file information of a Document entry: {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }


    @Transactional
    @Override
    public Link addLink(Long headId, Long tailId) {
        LOGGER.info("Adding new Link: ");

        return repository.addLink(headId, tailId);
    }

    @Transactional
    @Override
    public Link deleteLink(Long headId, Long tailId) {

        return repository.deleteLink(headId, tailId);
    }

    //todo remove this method
    @Transactional
    @Override
    public void setUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("Current Remote User - " + request.getRemoteUser());
        repository.setUser(request.getRemoteUser());

    }

    @Transactional
    @Override
    public void setUser(String userName) {
        LOGGER.info("Current User - "+userName);
        repository.setUser(userName);

        //jooq.execute("SELECT current_setting('my.username') FROM documents LIMIT 1;");
    }


    private Document createModel(DocumentDTO dto) {
        return Document.getBuilder(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .data(dto.getData())
                .id(dto.getId())
                .author(dto.getAuthor())
                .modifier(dto.getModifier())
                .fileLength(dto.getFileLength())
                .fileMimeType(dto.getFileMimeType())
                .fileName(dto.getFileName())
                .filePath(dto.getFilePath())
                .docVersion(dto.getDocVersion())
                .build();
    }

    @Override
    public Page<DocumentDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query) {
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
    public List<DocumentDTO> findAllByParent(final Long parentid) {
        LOGGER.info("Finding Documents by Type.");

        List<Document> docEntries = repository.findAllByParent(parentid);

        LOGGER.debug("Found {} Document entries.", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
    }
}
