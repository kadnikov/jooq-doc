package ru.doccloud.document.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ru.doccloud.common.DateHelper;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.DocumentSearchService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/restapi/systemdata")
public class IAController  extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IAController.class);

    private final DocumentCrudService crudService;

    private final DocumentSearchService searchService;
    
    public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    private final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(TIMESTAMP_PATTERN);
	

    @Autowired
    public IAController(DocumentCrudService crudService, DocumentSearchService searchService,
                              StorageAreaSettings storageAreaSettings, StorageManager storageManager) throws Exception {
        super(storageAreaSettings, storageManager, crudService);
        LOGGER.info("DocumentController(crudService={}, searchService = {}, storageAreaSettings= {}, storageManager={})", crudService, searchService, storageAreaSettings, storageManager);
        this.crudService = crudService;
        this.searchService = searchService;

        LOGGER.info("DocumentController(): storage settings {}", settingsNode);

    }

    @RequestMapping(value = "/tenants", method = RequestMethod.GET)
    public JsonNode findTenants() {
        LOGGER.info("findTenants");
        Long parentid = Long.parseLong("0");
        
        return getIaJson(crudService.findAllByParent(parentid),"tenants");
    }
    
    private JsonNode getIaJson(List<DocumentDTO> docs, String domain){
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode res = null;
    	String base_url = "http://localhost:8080/jooq";
    	String base = "{"
    			+ "\"_embedded\" : {"
    				+"\""+domain+"\" : []"
    			+ "},"
    			+ "\"_links\" : {"
	    			+ "\"self\" : {"
	    			+ "\"href\" : \""+base_url+"/restapi/systemdata/tenants\""
	    			+ "}"
    			+ "},"
    			+ "\"page\" : {"
	    			+"\"size\" : 10,"
	    			+"\"totalElements\" : 1,"
	    			+"\"totalPages\" : 1,"
	    			+"\"number\" : 0"
    			+ "}"
    			+ "}";
    	LOGGER.info(base);
    	
		try {
			res = mapper.readTree(base);
			LOGGER.info(mapper.writeValueAsString(res));
			ArrayNode nodes = (ArrayNode) res.path("_embedded").path(domain);
			for (DocumentDTO doc : docs){
				
				String item = "{"
						+ "\"permission\" : {"
							+"\"groups\" : []"
						+"},"
						+"\"_links\" : {}}";
				ObjectNode itemNode = (ObjectNode) mapper.readTree(item);
				itemNode.put("createdBy",doc.getAuthor());
				itemNode.put("createdDate",dateTimeFormat.print(doc.getCreationTime()));
				itemNode.put("lastModifiedBy",doc.getModifier());
				itemNode.put("lastModifiedDate",dateTimeFormat.print(doc.getModificationTime()));
				itemNode.put("version",doc.getDocVersion());
				itemNode.put("name",doc.getTitle());
				
				
				ObjectNode linksNode = (ObjectNode) itemNode.path("_links");
				ObjectNode sNode = mapper.createObjectNode();
				sNode.put("href",base_url+"/restapi/systemdata/"+domain+"/"+doc.getId());
				linksNode.put("self", sNode);
				
				ObjectNode jNode = mapper.createObjectNode();
				if (domain=="tenants"){
					jNode.put("href",base_url+"/restapi/systemdata/"+domain+"/"+doc.getId()+"/applications");
					linksNode.put("http://identifiers.emc.com/applications", jNode);
				}
				if (domain=="applications"){
					itemNode.put("structuredDataStorageAllocationStrategy","DEFAULT");
					itemNode.put("type", "APP_DECOMM");
					itemNode.put("archiveType", "TABLE");
					itemNode.put("searchCreated", true);
					itemNode.put("xdbLibraryAssociated", true);
					itemNode.put("state", "IN_TEST");
					itemNode.put("viewStatus", true);
					
					jNode.put("href",base_url+"/restapi/systemdata/"+domain+"/"+doc.getId()+"/searches");
					linksNode.put("http://identifiers.emc.com/searches", jNode);
				}
				LOGGER.info(mapper.writeValueAsString(itemNode));
				
				nodes.add(itemNode);
			}
			LOGGER.info(mapper.writeValueAsString(res));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
    }
    
    @RequestMapping(value = "/tenants/{tenantId}/applications", method = RequestMethod.GET)
    public JsonNode findAplications(@PathVariable("tenantId") String tenantId) {
        LOGGER.info("findApplications");
        Long parentid = Long.parseLong(tenantId);
        return getIaJson(crudService.findAllByParent(parentid),"applications");
    }
    
    @RequestMapping(value = "/type/{type}", method = RequestMethod.GET)
    public Page<DocumentDTO> findByType(@PathVariable("type") String type, @RequestParam(value = "fields",required=false) String fields, @RequestParam(value = "filters",required=false) String query,Pageable pageable) {
        LOGGER.info("findByType(type = {}, fields={}, query={}, pageSize= {}, pageNumber = {})",
                type, fields, query,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        String[] fieldsArr = null;
        if (fields!=null){
        	fieldsArr = fields.split(",");
        }

        return crudService.findAllByType(type, fieldsArr, pageable, query);
    }

    @RequestMapping(value = "/parent/{parentid}", method = RequestMethod.GET)
    public List<DocumentDTO> findByParent(@PathVariable("parentid") Long parentid) {
        LOGGER.info("findByParent(parentid = {})", parentid);

        return crudService.findAllByParent(parentid);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public DocumentDTO findById(@PathVariable("id") Long id) {
        LOGGER.info("findById(id= {})", id);

        return crudService.findById(id);
    }

    @RequestMapping(value = "/uuid/{uuid}", method = RequestMethod.GET)
    public DocumentDTO findByUUID(@PathVariable("uuid") String uuid) {
        LOGGER.info("findByUUID(uuid= {})", uuid);

        return crudService.findByUUID(uuid);
    }


}
