package ru.doccloud.document.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import ru.doccloud.common.dto.StorageAreaSettings;
import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.dto.LinkDTO;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;
import ru.doccloud.document.repository.DocumentRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
        this.repository = repository;
        this.transformer = transformer;
    }

    public DocumentRepository getRepository(){
        return this.repository;
    }

    @Transactional
    @Override
    public DocumentDTO add(final DocumentDTO dto, final String user) {
        LOGGER.debug("entering add(dto = {}, user = {})", dto, user);

        repository.setUser(user);
        dto.setAuthor(user);
        Document persisted = repository.add(createModel(dto));

        LOGGER.debug("leaving add(): Added Document entry {}", persisted);

        return transformer.convert(persisted, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO addToFolder(final DocumentDTO dto, final Long folderId) {
        LOGGER.debug("entering addToFolder(dto = {}, folderId={})", dto, folderId);

        Document persisted = null;
//        try to find document in database
        if(dto.getId() != null) {
            persisted = repository.findById(dto.getId());
        }

        if(persisted == null)
            persisted = repository.add(createModel(dto));

        Link link = repository.addLink(folderId, persisted.getId());

        LOGGER.debug("leaving addToFolder(): Added Document entry  {} with link {}", persisted, link);

        return transformer.convert(persisted, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO delete(final Long id) {
        LOGGER.debug("entering delete(id ={})", id);

        Document deleted = repository.delete(id);

        LOGGER.debug("leaving delete(): Deleted Document  {}", deleted);

        return transformer.convert(deleted, new DocumentDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public List<DocumentDTO> findAll() {
        LOGGER.debug("entering findAll() ");

        List<Document> docEntries = repository.findAll();

        LOGGER.debug("leaving findAll(): Found {} Documents", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DocumentDTO> findAll(final Pageable pageable, String query) {
        LOGGER.debug("entering findAll(pageable = {})", pageable);

        Page<Document> searchResults = repository.findAll(pageable, query);

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findAll(): Found {} Documents", searchResults.getNumber());

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }

    @Override
    public List<DocumentDTO> findParents(Long docId){
        LOGGER.debug("entering findParents(docId = {})", docId);
        final List<Document> docEntries = repository.findParents(docId);

        LOGGER.debug("leaving findParents(): Found: {}", docEntries);

        return docEntries == null ? null : transformer.convertList(docEntries, DocumentDTO.class);
    }


    @Transactional(readOnly = true)
    @Override
    public List<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable){
        LOGGER.debug("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);
        Page<Document> docPage = repository.findBySearchTerm(searchTerm, pageable);
        LOGGER.debug("leaving findBySearchTerm(): Found {}", docPage);
        return  transformer.convertList(docPage.getContent(), DocumentDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public DocumentDTO findById(final Long id) {
        LOGGER.debug("entering findById(id = {})", id);

        Document found = repository.findById(id);

        LOGGER.debug("leaving findById(): Found {}", found);

        return transformer.convert(found, new DocumentDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public DocumentDTO findByUUID(final String uuid) {
        LOGGER.debug("entering findByUUID(uuid = {})", uuid);

        Document found = repository.findByUUID(uuid);

        LOGGER.debug("leaving findByUUID(): Found {}", found);

        return transformer.convert(found, new DocumentDTO());
    }


    @Transactional(readOnly = true)
    @Override
    public JsonNode findSettings() {
        LOGGER.debug("entering findSettings()");
        JsonNode jsonNode = (JsonNode) StorageAreaSettings.INSTANCE.getStorageSetting();
        if(jsonNode == null) {
            LOGGER.debug("findSettings(): settinf=gs were not found in the cashe. Try to finf it in database");
            Document found = repository.findSettings();

            LOGGER.debug("leaving findSettings(): Found {}", found);
            jsonNode = found.getData();
            StorageAreaSettings.INSTANCE.add(found.getData());
        }

        return jsonNode;
    }


    @Transactional
    @Override
    public DocumentDTO update(final DocumentDTO dto, final String user) {
        LOGGER.debug("entering update(dto={}, user={})", dto, user);

        dto.setModifier(user);
        Document updated = repository.update(createModel(dto));

        LOGGER.debug("leaving update(): Updated {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO updateFileInfo(final DocumentDTO dto){
        LOGGER.debug("entering updateFileInfo(dto={})", dto);
        final Document updated = repository.updateFileInfo(createModel(dto));

        LOGGER.debug("leaving updateFileInfo(): Updated {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }

    @Transactional
    @Override
    public LinkDTO addLink(Long headId, Long tailId) {
        LOGGER.debug("entering addLink(headId={}, tailId = {})", headId, tailId);

        Link link = repository.addLink(headId, tailId);
        LOGGER.debug("leaving addLink(): Link {}", link);
        return transformer.convert(link, new LinkDTO());
    }
    @Transactional
    @Override
    public LinkDTO deleteLink(Long headId, Long tailId) {
        LOGGER.debug("entering deleteLink(headId={}, tailId = {})", headId, tailId);
        Link link = repository.deleteLink(headId, tailId);
        LOGGER.debug("leaving deleteLink(): Link {}", link);
        return transformer.convert(link, new LinkDTO());
    }

    @Transactional
    @Override
    public void setUser() {
        LOGGER.debug("entering setUser()");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("leaving setUser(): user {} ", request.getRemoteUser());
        repository.setUser(request.getRemoteUser());

    }

    @Transactional
    @Override
    public void setUser(String userName) {
        LOGGER.debug("setUser(userName={})", userName);
        repository.setUser(userName);
    }

    @Override
    public Page<DocumentDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query) {

        LOGGER.debug("entering findAllByType(type={}, fields={}, pageable={}, query={})", type, fields, pageable, query);
        Page<Document> searchResults = repository.findAllByType(type, fields, pageable, query);

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findAllByType(): Found {} Documents", searchResults.getNumber());
        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }

    @Override
    public List<DocumentDTO> findAllByParent(final Long parentid) {
        LOGGER.debug("entering findAllByParent(parentId = {})", parentid);

        List<Document> docEntries = repository.findAllByParent(parentid);

        LOGGER.debug("leaving findAllByParent(): Found {} Documents", docEntries);

        return transformer.convertList(docEntries, DocumentDTO.class);
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
}
