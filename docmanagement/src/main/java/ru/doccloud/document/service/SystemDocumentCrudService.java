package ru.doccloud.document.service;

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
import ru.doccloud.document.dto.SystemDTO;
import ru.doccloud.document.model.SystemEntity;
import ru.doccloud.document.repository.SystemRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Andrey Kadnikov
 */
@Service
public class SystemDocumentCrudService implements SystemCrudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemDocumentCrudService.class);

    private final SystemRepository repository;

    private final JTransfo transformer;

    @Autowired
    public SystemDocumentCrudService(SystemRepository repository, JTransfo transformer) {
        this.repository = repository;
        this.transformer = transformer;
    }

    public SystemRepository getRepository(){
        return this.repository;
    }

    @Transactional
    @Override
    public SystemDTO add(final SystemDTO dto, final String user) {
        LOGGER.debug("entering add(dto = {}, user = {})", dto, user);

        repository.setUser(user);
        dto.setAuthor(user);
        SystemEntity persisted = repository.add(createModel(dto));

        LOGGER.debug("leaving add(): Added Document entry {}", persisted);

        return transformer.convert(persisted, new SystemDTO());
    }

//    @Transactional
//    @Override
//    public SystemDTO addToFolder(final SystemDTO dto, final Long folderId) {
//        LOGGER.debug("entering addToFolder(dto = {}, folderId={})", dto, folderId);
//
//        Document persisted = null;
////        try to find document in database
//        if(dto.getId() != null) {
//            persisted = repository.findById(dto.getId());
//        }
//
//        if(persisted == null)
//            persisted = repository.add(createModel(dto));
//
//        Link link = repository.addLink(folderId, persisted.getId());
//
//        LOGGER.debug("leaving addToFolder(): Added Document entry  {} with link {}", persisted, link);
//
//        return transformer.convert(persisted, new SystemDTO());
//    }

    @Transactional
    @Override
    public SystemDTO delete(final Long id) {
        LOGGER.debug("entering delete(id ={})", id);

        SystemEntity deleted = repository.delete(id);

        LOGGER.debug("leaving delete(): Deleted Document  {}", deleted);

        return transformer.convert(deleted, new SystemDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public List<SystemDTO> findAll() {
        LOGGER.debug("entering findAll() ");

        List<SystemEntity> docEntries = repository.findAll();

        LOGGER.debug("leaving findAll(): Found {} Documents", docEntries.size());

        return transformer.convertList(docEntries, SystemDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SystemDTO> findAll(final Pageable pageable, String query) {
        LOGGER.debug("entering findAll(pageable = {})", pageable);

        Page<SystemEntity> searchResults = repository.findAll(pageable, query);

        List<SystemDTO> dtos = transformer.convertList(searchResults.getContent(), SystemDTO.class);

        LOGGER.debug("leaving findAll(): Found {} Documents", searchResults.getNumber());

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }



    @Transactional(readOnly = true)
    @Override
    public List<SystemDTO> findBySearchTerm(String searchTerm, Pageable pageable){
        LOGGER.debug("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);
        Page<SystemEntity> docPage = repository.findBySearchTerm(searchTerm, pageable);
        LOGGER.debug("leaving findBySearchTerm(): Found {}", docPage);
        return  transformer.convertList(docPage.getContent(), SystemDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public SystemDTO findById(final Long id) {
        LOGGER.debug("entering findById(id = {})", id);

        SystemEntity found = repository.findById(id);

        LOGGER.debug("leaving findById(): Found {}", found);

        return transformer.convert(found, new SystemDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public SystemDTO findByUUID(final String uuid) {
        LOGGER.debug("entering findByUUID(uuid = {})", uuid);

        SystemEntity found = repository.findByUUID(uuid);

        LOGGER.debug("leaving findByUUID(): Found {}", found);

        return transformer.convert(found, new SystemDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public SystemDTO findBySymbolicName(final String symbolic) {
        LOGGER.debug("entering findByUUID(uuid = {})", symbolic);

        SystemEntity found = repository.findBySymbolicName(symbolic);

        LOGGER.debug("leaving findBySymbolicName(): Found {}", found);

        return transformer.convert(found, new SystemDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public SystemDTO findSettings() {
        LOGGER.debug("entering findSettings()");
        SystemEntity found = repository.findSettings();

        LOGGER.debug("leaving findSettings(): Found {}", found);

        return transformer.convert(found, new SystemDTO());
    }


    @Transactional
    @Override
    public SystemDTO update(final SystemDTO dto, final String user) {
        LOGGER.debug("entering update(dto={}, user={})", dto, user);

        dto.setModifier(user);
        SystemEntity updated = repository.update(createModel(dto));

        LOGGER.debug("leaving update(): Updated {}", updated);

        return transformer.convert(updated, new SystemDTO());
    }

    @Transactional
    @Override
    public SystemDTO updateFileInfo(final SystemDTO dto){
        LOGGER.debug("entering updateFileInfo(dto={})", dto);
        final SystemEntity updated = repository.updateFileInfo(createModel(dto));

        LOGGER.debug("leaving updateFileInfo(): Updated {}", updated);

        return transformer.convert(updated, new SystemDTO());
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
    public Page<SystemDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query) {

        LOGGER.debug("entering findAllByType(type={}, fields={}, pageable={}, query={})", type, fields, pageable, query);
        Page<SystemEntity> searchResults = repository.findAllByType(type, fields, pageable, query);

        List<SystemDTO> dtos = transformer.convertList(searchResults.getContent(), SystemDTO.class);

        LOGGER.debug("leaving findAllByType(): Found {} Documents", searchResults.getNumber());
        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }


    private SystemEntity createModel(SystemDTO dto) {
        return SystemEntity.getBuilder(dto.getTitle())
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
