package ru.doccloud.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.document.model.SystemDocument;
import ru.doccloud.repository.SystemRepository;
import ru.doccloud.service.SystemCrudService;

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
        SystemDocument persisted = repository.add(createModel(dto));

        LOGGER.debug("leaving add(): Added Document entry {}", persisted);

        return transformer.convert(persisted, new SystemDTO());
    }


    @Transactional
    @Override
    public SystemDTO delete(final Long id) {
        LOGGER.debug("entering delete(id ={})", id);

        SystemDocument deleted = repository.delete(id);

        LOGGER.debug("leaving delete(): Deleted Document  {}", deleted);

        return transformer.convert(deleted, new SystemDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public List<SystemDTO> findAll() {
        LOGGER.debug("entering findAll() ");

        List<SystemDocument> docEntries = repository.findAll();

        LOGGER.debug("leaving findAll(): Found {} Documents", docEntries.size());

        return transformer.convertList(docEntries, SystemDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SystemDTO> findAll(final Pageable pageable, String query) {
        LOGGER.debug("entering findAll(pageable = {})", pageable);

        Page<SystemDocument> searchResults = repository.findAll(pageable, query);

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
        Page<SystemDocument> docPage = repository.findBySearchTerm(searchTerm, pageable);
        LOGGER.debug("leaving findBySearchTerm(): Found {}", docPage);
        return  transformer.convertList(docPage.getContent(), SystemDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public SystemDTO findById(final Long id) {
        LOGGER.debug("entering findById(id = {})", id);

        SystemDocument found = repository.findById(id);

        LOGGER.debug("leaving findById(): Found {}", found);

        return transformer.convert(found, new SystemDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public SystemDTO findByUUID(final String uuid) {
        LOGGER.debug("entering findByUUID(uuid = {})", uuid);

        SystemDocument found = repository.findByUUID(uuid);

        LOGGER.debug("leaving findByUUID(): Found {}", found);

        return transformer.convert(found, new SystemDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public SystemDTO findBySymbolicName(final String symbolic) {
        LOGGER.debug("entering findByUUID(uuid = {})", symbolic);

        SystemDocument found = repository.findBySymbolicName(symbolic);
        ObjectNode data = (ObjectNode) found.getData();
        if (found.getType().equals("type")){
        	JsonNode schemaNode = data.get("schema");
        	schemaNode = addParentSchema(found, schemaNode);
        	data.put("schema", schemaNode);
        }

        LOGGER.debug("leaving findBySymbolicName(): Found {}", found);
        SystemDTO res = transformer.convert(found, new SystemDTO());
        res.setData(data);
        return res;
    }
    
    private JsonNode addParentSchema(SystemDocument typedoc, JsonNode schemaNode) {
    	if (typedoc.getParent()!=null)
		if (!typedoc.getParent().equals("0")){
			LOGGER.info("Parent - {}",typedoc.getParent());
			SystemDocument parenttype = repository.findById(Long.parseLong(typedoc.getParent()));
	        if (parenttype!=null){
	        	JsonNode parentSchema = parenttype.getData().get("schema");
	        	ObjectNode props=(ObjectNode) schemaNode.get("properties");
	        	ObjectNode parentprops=(ObjectNode) parentSchema.get("properties");
	        	props.setAll(parentprops);
	        	ObjectMapper mapper = new ObjectMapper();
	        	try {
					LOGGER.info("schemaNode - {}",mapper.writeValueAsString(schemaNode));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
	        	schemaNode = addParentSchema(parenttype, schemaNode);
	        }
	        
		}
		return schemaNode;
	}

    @Transactional(readOnly = true)
    @Override
    public JsonNode findSettings(final String settingsKey) {
        LOGGER.debug("entering findSettings(settingsKey={})", settingsKey);
        SystemDocument found = repository.findSettings(settingsKey);
        LOGGER.debug("leaving findSettings(): Found {}", found);

        return found.getData();
    }


    @Transactional
    @Override
    public SystemDTO update(final SystemDTO dto, final String user) {
        LOGGER.debug("entering update(dto={}, user={})", dto, user);

        dto.setModifier(user);
        SystemDocument updated = repository.update(createModel(dto));

        LOGGER.debug("leaving update(): Updated {}", updated);

        return transformer.convert(updated, new SystemDTO());
    }

    @Transactional
    @Override
    public SystemDTO updateFileInfo(final SystemDTO dto){
        LOGGER.debug("entering updateFileInfo(dto={})", dto);
        final SystemDocument updated = repository.updateFileInfo(createModel(dto));

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
        Page<SystemDocument> searchResults = repository.findAllByType(type, fields, pageable, query);

        List<SystemDTO> dtos = transformer.convertList(searchResults.getContent(), SystemDTO.class);

        LOGGER.debug("leaving findAllByType(): Found {} Documents", searchResults.getNumber());
        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }
    
    @Override
    public Page<SystemDTO> findAllByParentAndType(final Long parentid, String type, final Pageable pageable) {
        LOGGER.debug("entering findAllByParentAndType(parentId = {}, type = {})", parentid, type); 

        Page<SystemDocument> searchResults = repository.findAllByParentAndType(parentid, type, pageable);

        List<SystemDTO> dtos = transformer.convertList(searchResults.getContent(), SystemDTO.class);

        LOGGER.debug("leaving findAllByParentAndType(): Found {} Documents", dtos);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }


    private SystemDocument createModel(SystemDTO dto) {
        return SystemDocument.getBuilder(dto.getTitle())
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
                .symbolicName(dto.getSymbolicName())
                .parent(dto.getParent())
                .build();
    }
}
