package ru.doccloud.document.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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
    private static final String base_url = "http://localhost:8080/jooq";
    
    private final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(TIMESTAMP_PATTERN);
	

    @Autowired
    public IAController(DocumentCrudService crudService, DocumentSearchService searchService,
                              StorageAreaSettings storageAreaSettings, StorageManager storageManager) throws Exception {
        super(storageAreaSettings, storageManager, crudService);
        LOGGER.info("DocumentController(crudService={}, searchService = {}, storageAreaSettings= {}, storageManager={})", crudService, searchService, storageAreaSettings, storageManager);
        this.crudService = crudService;
        this.searchService = searchService;

        LOGGER.info("DocumentController(): storage settings {}", storageSettingsNode);

    }

    @RequestMapping(value = "/tenants", method = RequestMethod.GET)
    public JsonNode findTenants(Pageable pageable) {
        LOGGER.info("findTenants");
        Long parentid = Long.parseLong("0");
        
        return getIaJson(crudService.findAllByParentAndType(parentid,"tenant",pageable),"tenants");
    }
    private JsonNode getXForm(DocumentDTO doc){
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode res = null;
    	String base_url = "http://localhost:8080/jooq";
    	String base = "{"
    			+ "\"_links\" : {"
	    			+ "\"self\" : {"
	    			+ "\"href\" : \""+base_url+"/restapi/systemdata/xforms\""
	    			+ "}"
    			+ "}"
    			+ "}";
    	try {
    		res = (ObjectNode) mapper.readTree(base);
			res.put("lastModifiedBy",doc.getModifier());
			res.put("lastModifiedDate",dateTimeFormat.print(doc.getModificationTime()));
			res.put("version",doc.getDocVersion());
			
			final String filePath = doc.getFilePath();
	        if(StringUtils.isBlank(filePath)) {
	            LOGGER.error("Filepath is empty. Content for document {} does not exist", doc);
	            throw new Exception("Filepath is empty, content for document " + doc + "does not exist");
	        }
	        byte[] file = storageActionsService.readFile(filePath);
	        String fileString = new String(file);
	        res.put("form", fileString);

    	} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return res;
			
    }
    private JsonNode getIaJson(Page<DocumentDTO> docs, String domain){
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode res = null;
    	String base = "{"
    			+ "\"_embedded\" : {"
    				+"\""+domain+"\" : []"
    			+ "},"
    			+ "\"_links\" : {"
	    			+ "\"self\" : {"
	    			+ "\"href\" : \""+base_url+"/restapi/systemdata/"+domain+"\""
	    			+ "}"
    			+ "},"
    			+ "\"page\" : {"
	    			+"\"size\" : "+docs.getSize()+","
	    			+"\"totalElements\" : "+docs.getTotalElements()+","
	    			+"\"totalPages\" : "+docs.getTotalPages()+","
	    			+"\"number\" : "+docs.getNumber()
    			+ "}"
    			+ "}";
    	
        
    	LOGGER.info(base);
    	
		try {
			res = mapper.readTree(base);
			LOGGER.info(mapper.writeValueAsString(res));
			ArrayNode nodes = (ArrayNode) res.path("_embedded").path(domain);
			ObjectNode itemNode = null;
			for (DocumentDTO doc : docs){
				itemNode = getIaJsonObj(doc, domain);
				nodes.add(itemNode);
			}
		
			LOGGER.info(mapper.writeValueAsString(res));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
    }
    
    private JsonNode getSearchResultJson(Page<DocumentDTO> docs, String[] docFields){
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode res = null;
    	String base = "{"
    			+ "\"_embedded\" : {"
    				+"\"results\" : []"
    			+ "},"
    			+ "\"_links\" : {"
	    			+ "\"self\" : {"
	    			+ "\"href\" : \""+base_url+"/restapi/systemdata/results\""
	    			+ "}"
    			+ "},"
    			+ "\"page\" : {"
	    			+"\"size\" : "+docs.getSize()+","
	    			+"\"totalElements\" : "+docs.getTotalElements()+","
	    			+"\"totalPages\" : "+docs.getTotalPages()+","
	    			+"\"number\" : "+docs.getNumber()
    			+ "}"
    			+ "}";
    	
        
    	LOGGER.info(base);
    	
		try {
			res = mapper.readTree(base);
			LOGGER.info(mapper.writeValueAsString(res));
			ArrayNode nodes = (ArrayNode) res.path("_embedded").path("results");
			ObjectNode resNode = (ObjectNode) mapper.readTree("{\"rows\":[]}");
			resNode.put("totalElements", docs.getTotalElements());
			resNode.put("empty", false);
			resNode.put("executionTime", 100);
			
			ArrayNode rows = (ArrayNode) resNode.path("rows");
			for (DocumentDTO doc : docs){
				ObjectNode itemNode = getSearchResultJsonObj(doc, docFields);
				rows.add(itemNode);
			}
			nodes.add(resNode);

			LOGGER.info(mapper.writeValueAsString(res));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
    }
    private ObjectNode getSearchResultJsonObj(DocumentDTO doc, String[] docFields) throws JsonProcessingException, IOException{
    	ObjectMapper mapper = new ObjectMapper();

		ObjectNode itemNode = (ObjectNode) mapper.readTree("{\"columns\":[]}");
		itemNode.put("id", doc.getId());
		ArrayNode columns = (ArrayNode) itemNode.path("columns");
		
		for (String field: docFields){
			ObjectNode colNode = (ObjectNode) mapper.readTree("{}");
			colNode.put("name", field);
			colNode.put("cid", false);
			if (doc.getData()!=null && doc.getData().get(field)!=null){
				colNode.put("value", doc.getData().get(field).textValue());
			}else{
				colNode.put("value","");
			}
			columns.add(colNode);
		}
		return itemNode;
    }
    private ObjectNode getIaJsonObj(DocumentDTO doc, String domain) throws JsonProcessingException, IOException{
    	ObjectMapper mapper = new ObjectMapper();
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
		if (domain=="searches"){
			jNode.put("href",base_url+"/restapi/systemdata/"+domain+"/"+doc.getId()+"/search-compositions");
			linksNode.put("http://identifiers.emc.com/search-compositions", jNode);
			itemNode.put("description", doc.getDescription());
			itemNode.put("nestedSearch",false);
			itemNode.put("state","PUBLISHED");
			itemNode.put("inUse",true);
		}
		if (domain=="searchCompositions"){
			jNode.put("href",base_url+"/restapi/systemdata/xforms/"+doc.getId());
			linksNode.put("http://identifiers.emc.com/xform", jNode);
			ObjectNode jNode1 = mapper.createObjectNode();
			jNode1.put("href",base_url+"/restapi/systemdata/result-masters/"+doc.getId());
			linksNode.put("http://identifiers.emc.com/result-master", jNode1);
			itemNode.put("searchName", doc.getDescription());
		}
		if (domain=="panels"){
			ObjectNode panelNode = (ObjectNode) mapper.readTree("{\"tabs\":[]}");
			panelNode.put("name", "Main Panel");
			panelNode.put("title", "null");
			panelNode.put("description", "null");
			ArrayNode tabsnodes = (ArrayNode) panelNode.path("tabs");
			ObjectNode tabNode = (ObjectNode) mapper.readTree("{}");
			tabNode.put("name", "_ia_Default_Main_tab_");
			tabNode.put("title", "null");
			tabNode.put("description", "null");
			ArrayNode colsnodes = tabNode.putArray("columns");
			colsnodes.addAll((ArrayNode) doc.getData().get("fields"));
			
//			ArrayNode colsnodes = (ArrayNode) tabNode.path("columns");
			
//			String[] docFields = doc.getData().get("fields").textValue().split(",");
//	        for (String field: docFields){
//	        	ObjectNode colNode = (ObjectNode) mapper.readTree("{}");
//				colNode.put("name", field);
//	        	colNode.put("label", field);
//	        	colNode.put("dataType", "STRING");
//	        	colNode.put("hidden", false);
//	        	colNode.put("sortable", true);
//	        	colsnodes.add(colNode);
//	        }
			
			tabsnodes.add(tabNode);
			
			ArrayNode panels = itemNode.putArray("panels");
			panels.add(panelNode);
		}

		return itemNode;
    }
    
    @RequestMapping(value = "/tenants/{tenantId}/applications", method = RequestMethod.GET)
    public JsonNode findAplications(@PathVariable("tenantId") String tenantId,Pageable pageable) {
        LOGGER.info("findApplications");
        Long parentid = Long.parseLong(tenantId);
        return getIaJson(crudService.findAllByParentAndType(parentid, "application",pageable),"applications"); 
    }
    
    @RequestMapping(value = "/applications/{appId}", method = RequestMethod.GET)
    public JsonNode getApplication(@PathVariable("appId") String appId) {
        LOGGER.info("findApplications");
        Long id = Long.parseLong(appId);
        ObjectNode res = null;
        try {
			res = getIaJsonObj(crudService.findById(id),"applications");
		} catch (IOException e) {
			e.printStackTrace();
		}
        return res;
    }
    
    @RequestMapping(value = "/applications/{appId}/searches", method = RequestMethod.GET)
    public JsonNode findSearches(@PathVariable("appId") String appId,Pageable pageable) {
        LOGGER.info("findSearches");
        Long parentid = Long.parseLong(appId);
        return getIaJson(crudService.findAllByParentAndType(parentid, "search",pageable),"searches");
    }
    
    @RequestMapping(value = "/searches/{searchId}", method = RequestMethod.GET)
    public JsonNode getSearch(@PathVariable("searchId") String searchId) {
        LOGGER.info("getSearch");
        Long id = Long.parseLong(searchId);
        ObjectNode res = null;
        try {
			res = getIaJsonObj(crudService.findById(id),"searches");
		} catch (IOException e) {
			e.printStackTrace();
		}
        return res;
    }
    
    @RequestMapping(value = "/searches/{searchId}/search-compositions", method = RequestMethod.GET)
    public JsonNode findSearchCompositions(@PathVariable("searchId") String searchId,Pageable pageable) {
        LOGGER.info("findApplications");
        Long parentid = Long.parseLong(searchId);
        return getIaJson(crudService.findAllByParentAndType(parentid, "search-composition",pageable),"searchCompositions");
    }
    
    @RequestMapping(value = "/search-compositions/{searchCompId}", method = RequestMethod.GET)
    public JsonNode getSearchComposition(@PathVariable("searchCompId") String searchCompId) {
        LOGGER.info("getSearchComposition");
        Long id = Long.parseLong(searchCompId);
        ObjectNode res = null;
        try {
			res = getIaJsonObj(crudService.findById(id),"searchCompositions");
		} catch (IOException e) {
			e.printStackTrace();
		}
        return res;
    }
    
    @RequestMapping(value = "/result-masters/{searchCompId}", method = RequestMethod.GET)
    public JsonNode getReultMaster(@PathVariable("searchCompId") String searchCompId) {
        LOGGER.info("getSearchComposition");
        Long id = Long.parseLong(searchCompId);
        ObjectNode res = null;
        try {
			res = getIaJsonObj(crudService.findById(id),"panels");
		} catch (IOException e) {
			e.printStackTrace();
		}
        return res;
    }
    
    @RequestMapping(value = "/test", 
    		method = RequestMethod.POST)
    public JsonNode testRequest(HttpServletRequest request) {
    	JsonNode res = null;
    	try {
	    	String body = IOUtils.toString( request.getInputStream());
	        LOGGER.info("test Request, Body = "+body);
	        ObjectMapper mapper = new ObjectMapper();
        	res = mapper.readTree("{}");
		} catch (IOException e) {
			e.printStackTrace();
		}
        return res;
    }
    @RequestMapping(value = "/search-compositions/{searchCompId}", 
    		method = RequestMethod.POST)
    public JsonNode processSearchRequest(@PathVariable("searchCompId") String searchCompId, HttpServletRequest request,Pageable pageable ) {
        LOGGER.info("processSearchRequest");
        ObjectMapper mapper = new ObjectMapper();
        Long id = Long.parseLong(searchCompId);
        DocumentDTO searchDoc = crudService.findById(id);
        String docType = searchDoc.getData().get("type").textValue();
        
        ArrayNode fieldsArr = (ArrayNode) searchDoc.getData().get("fields");
        List<String> fieldsNameList = new ArrayList<>();
        for (JsonNode node : fieldsArr) {
        	fieldsNameList.add(node.get("name").asText());
        }
        String[] docFields = fieldsNameList.toArray(new String[0]);
    	JsonNode res = null;
    	try {
    		ObjectNode params = (ObjectNode) mapper.readTree("{}");

			ArrayNode nodes = (ArrayNode) mapper.readTree("[]");
			
			XmlMapper xmlMapper = new XmlMapper();
			String body = IOUtils.toString( request.getInputStream());
			System.out.println(body);
		    Data value = xmlMapper.readValue(body, Data.class);
		    queryParam[] qparams = value.getCriterions();
		    for (queryParam par : qparams){
		    	System.out.println(par.getField()+" - "+par.getValue());
		    
				if (par.getValue()!=null){
					ObjectNode itemNode = (ObjectNode) mapper.readTree("{}");
					itemNode.put("field", par.getField());
					itemNode.put("op", "cn");
					itemNode.put("data", par.getValue());
					nodes.add(itemNode);
				}
				
			}
			params.put("groupOp", "AND");
			params.put("rules", nodes);
			
			String searchParamsJson = params.toString();
			LOGGER.info("searchParamsJson - {}", searchParamsJson);
			
			res = getSearchResultJson((Page<DocumentDTO>) crudService.findAllByType(docType, docFields, pageable, searchParamsJson), docFields);
    	} catch (IOException e) {
			e.printStackTrace();
		}
        return res;
    }
    
    @RequestMapping(value = "/xforms/{searchCompId}", method = RequestMethod.GET)
    public JsonNode getSearchXForm(@PathVariable("searchCompId") String searchCompId) {
        LOGGER.info("getSearchXForm");
        Long id = Long.parseLong(searchCompId);
        return getXForm(crudService.findById(id));
    }


}
