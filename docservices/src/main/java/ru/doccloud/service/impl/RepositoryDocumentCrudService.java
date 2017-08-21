package ru.doccloud.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;
import ru.doccloud.document.model.SystemDocument;
import ru.doccloud.repository.DocumentRepository;
import ru.doccloud.repository.SystemRepository;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.LinkDTO;

/**
 * @author Andrey Kadnikov
 */
@Service
public class RepositoryDocumentCrudService implements DocumentCrudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDocumentCrudService.class);

    private final DocumentRepository repository;

    private final SystemRepository sysRepository;
    
    private final JTransfo transformer;

    @Autowired
    public RepositoryDocumentCrudService(DocumentRepository repository, SystemRepository sysRepository, JTransfo transformer) {
        this.repository = repository;
        this.sysRepository = sysRepository;
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
        
        List<String> readersArr = new ArrayList<String>();
        readersArr.add(user);
        
        SystemDocument typedoc = sysRepository.findBySymbolicName(dto.getType());
        if (typedoc!=null){
	        ArrayNode accessFromType = (ArrayNode) typedoc.getData().get("access");
	        
	        if (accessFromType.isArray()){
		        for (JsonNode acc: accessFromType){
		        	LOGGER.debug("reader - {}",acc.asText());
		        	readersArr.add(acc.asText());
		        }
	        }
	        validateSchema(typedoc,dto);
	        
        }
        
        String[] readers = readersArr.toArray(new String[0]);
        LOGGER.debug("add(): readers {}", readers);
        dto.setReaders(readersArr);
        
        if (dto.getBaseType() == null) dto.setBaseType("document");
        
        LOGGER.debug("dto = {}", dto);
        LOGGER.debug("documentEntry= {}", createModel(dto));
        Document persisted = repository.add(createModel(dto));

        LOGGER.debug("leaving add(): Added Document entry {}", persisted);

        return transformer.convert(persisted, new DocumentDTO());
    }
    
    private void validateSchema(SystemDocument typedoc, DocumentDTO dto){
    	JsonNode schemaNode = typedoc.getData().get("schema");
    	schemaNode = addParentSchema(typedoc, schemaNode);
        if (!schemaNode.isNull()){
        	ObjectMapper mapper = new ObjectMapper();
        	try {
        		LOGGER.debug("Schema - {}",mapper.writeValueAsString(schemaNode));
	        	JSONObject rawSchema = new JSONObject(mapper.writeValueAsString(schemaNode));
	        	Schema schema = SchemaLoader.load(rawSchema);
	        	LOGGER.debug("Data - {}",mapper.writeValueAsString(dto.getData()));
		        schema.validate(new JSONObject(mapper.writeValueAsString(dto.getData())));
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
	        
        }
    
    }

    private JsonNode addParentSchema(SystemDocument typedoc, JsonNode schemaNode) {
    	if (typedoc.getParent()!=null)
		if (!typedoc.getParent().equals("0")){
			LOGGER.info("Parent - {}",typedoc.getParent());
			SystemDocument parenttype = sysRepository.findById(Long.parseLong(typedoc.getParent()));
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

        dto.setParent(folderId.toString());
        setParent(dto);
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

//todo remove this use only RepositoryDocumentSearchService
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

    @Transactional
    @Override
    public DocumentDTO update(final DocumentDTO dto, final String user) {
        LOGGER.debug("entering update(dto={}, user={})", dto, user);

        dto.setModifier(user);
        SystemDocument typedoc = sysRepository.findBySymbolicName(dto.getType());
        if (typedoc!=null){
	        validateSchema(typedoc,dto);
	        
        }
        List<String> readersArr = new ArrayList<String>();
        readersArr.add(user);
        dto.setReaders(readersArr);
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
    public DocumentDTO setParent(final DocumentDTO dto){
        LOGGER.debug("entering updateFileInfo(dto={})", dto);
        final Document updated = repository.setParent(createModel(dto));

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
        repository.setUser(getRequestUser());

    }
    
    private String getRequestUser(){
    	HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("leaving setUser(): user {} ", request.getRemoteUser());
		return request.getRemoteUser();
        
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
        Page<Document> searchResults = repository.findAllByType(type, fields, pageable, query, getRequestUser());

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
    
    @Override
    public Page<DocumentDTO> findAllByParentAndType(final Long parentid, String type, final Pageable pageable) {
        LOGGER.debug("entering findAllByParentAndType(parentId = {}, type = {})", parentid, type); 

        Page<Document> searchResults = repository.findAllByParentAndType(parentid, type, pageable);

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findAllByParentAndType(): Found {} Documents", dtos);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }

    private Document createModel(DocumentDTO dto) {
        return Document.getBuilder(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .baseType(dto.getBaseType())
                .parent(dto.getParent())
                .readers(dto.getReaders().toArray(new String[0]))
                .data(dto.getData())
                .id(dto.getId())
                .author(dto.getAuthor())
                .modifier(dto.getModifier())
                .fileLength(dto.getFileLength())
                .fileMimeType(dto.getFileMimeType())
                .fileName(dto.getFileName())
                .filePath(dto.getFilePath())
                .docVersion(dto.getDocVersion())
                .fileStorage(dto.getFileStorage())
                .build();
    }
}
