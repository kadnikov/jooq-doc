package ru.doccloud.document.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.common.util.VersionHelper;
import ru.doccloud.document.dto.SystemDTO;
import ru.doccloud.amazon.service.SystemCrudService;
import ru.doccloud.storage.StorageActionsService;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;
import ru.doccloud.storagemanager.Storages;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/api/system")
public class SystemController  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemController.class);

    private final SystemCrudService crudService;

    private final StorageAreaSettings storageAreaSettings;
    private final StorageActionsService storageActionsService;
    private final StorageManager storageManager;

    private JsonNode settingsNode;

    @Autowired
    public SystemController(SystemCrudService crudService, StorageAreaSettings storageAreaSettings, StorageManager storageManager) throws Exception {
        LOGGER.info("SystemController(crudService={}, storageAreaSettings= {}, storageManager={})", crudService, storageAreaSettings, storageManager);
        this.crudService = crudService;
        this.storageAreaSettings = storageAreaSettings;
        settingsNode = (JsonNode) storageAreaSettings.getStorageSetting();
        LOGGER.info("SystemController(): storage settings {}", settingsNode);
        this.storageManager = storageManager;
        this.storageActionsService = storageManager.getStorageService(getDefaultStorage());
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public SystemDTO add(HttpServletRequest request, @RequestBody @Valid SystemDTO dto) {
        LOGGER.info("add(): add new document");

        return addDoc(dto, request.getRemoteUser());
    }


    @RequestMapping(value="/addcontent",headers="content-type=multipart/*",method=RequestMethod.POST)
    public SystemDTO addContent(MultipartHttpServletRequest request) throws Exception {

        LOGGER.info("addContent... add new document from request uri {}", request.getRequestURI());
        Enumeration<String> headerNames = request.getParameterNames();//.getAttribute("data");
        
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				LOGGER.debug("Header: "+ headerName+ " - " + request.getHeader(headerName));
		            }
		    }
        SystemDTO dto = new SystemDTO();
        String data = (String) request.getParameter("data");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jdata = mapper.readTree(data);
        String title = jdata.get("title").asText();
        String type = jdata.get("type").asText();
        dto.setTitle(title);
        dto.setType(type);
        Iterator<String> itr =  request.getFileNames();
        if(!itr.hasNext())
            return addDoc(dto, request.getRemoteUser());
        MultipartFile mpf = request.getFile(itr.next());
        initFileParamsFromRequest(dto, mpf);

       return writeContent(addDoc(dto, request.getRemoteUser()), mpf, request.getRemoteUser());
    }


    @RequestMapping(value="/updatecontent/{id}",headers="content-type=multipart/*",method=RequestMethod.PUT)
    public SystemDTO updateContent(MultipartHttpServletRequest request,  @PathVariable("id") Long id) throws Exception {
        LOGGER.info("updateContent... update document with id {} from request uri {}", id, request.getRequestURI());

        SystemDTO dto = crudService.findById(id);

        if(dto == null)
            throw new Exception("The document with such id " + id + " was not found in database ");

        Iterator<String> itr =  request.getFileNames();

        MultipartFile mpf = request.getFile(itr.next());

        return writeContent(dto, mpf, request.getRemoteUser());
    }

    @RequestMapping(value="/getcontent/{id}",headers="content-type=multipart/*",method=RequestMethod.GET)
    public byte[] getContent( @PathVariable("id") Long id) throws Exception {

        LOGGER.info("getContent (id = {})", id);
        SystemDTO dto = crudService.findById(id);

        if(dto == null)
            throw new Exception("The document with such id " + id + " was not found in database ");

        LOGGER.info("getContent(): Found Document with id: {}", dto.getId());

        final String filePath = dto.getFilePath();
        if(StringUtils.isBlank(filePath)) {
            LOGGER.error("Filepath is empty. Content for document {} does not exist", dto);
            throw new Exception("Filepath is empty, conteny for document " + dto + "does not exist");
        }

        return storageActionsService.readFile(filePath);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public SystemDTO delete(@PathVariable("id") Long id) {
        LOGGER.info("delete(id={})", id);

        return crudService.delete(id);
    }

    /**@RequestMapping(method = RequestMethod.GET)
    public List<DocumentDTO> findAll() {
        LOGGER.info("Finding all Document entries");

        List<DocumentDTO> documentEntries = crudService.findAll();

        LOGGER.info("Found {} Document entries.");

        return documentEntries;
    }*/
    
    @RequestMapping(method = RequestMethod.GET)
    public Page<SystemDTO> findAll(Pageable pageable, @RequestParam(value = "filters",required=false) String query) {
        LOGGER.info("findAll(pageSize= {}, pageNumber = {}) ",
                pageable.getPageSize(),
                pageable.getPageNumber()
        );

        return crudService.findAll(pageable, query);
    }
    
    @RequestMapping(value = "/type/{type}", method = RequestMethod.GET)
    public Page<SystemDTO> findByType(@PathVariable("type") String type, @RequestParam(value = "fields",required=false) String fields, @RequestParam(value = "filters",required=false) String query,Pageable pageable) {
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
    
    @RequestMapping(value = "/types", method = RequestMethod.GET)
    public Page<SystemDTO> getTypes(@RequestParam(value = "fields",required=false) String fields, @RequestParam(value = "filters",required=false) String query,Pageable pageable) {
        LOGGER.info("findByType(type = {}, fields={}, query={}, pageSize= {}, pageNumber = {})",
                "type", fields, query,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        String[] fieldsArr = null;
        if (fields!=null){
        	fieldsArr = fields.split(",");
        }

        return crudService.findAllByType("type", fieldsArr, pageable, query);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public SystemDTO findById(@PathVariable("id") Long id) {
        LOGGER.info("findById(id= {})", id);

        return crudService.findById(id);
    }

    @RequestMapping(value = "/uuid/{uuid}", method = RequestMethod.GET)
    public SystemDTO findByUUID(@PathVariable("uuid") String uuid) {
        LOGGER.info("findByUUID(uuid= {})", uuid);

        return crudService.findByUUID(uuid);
    }

    @RequestMapping(value = "/s/{symbolic}", method = RequestMethod.GET)
    public SystemDTO findBySymbolicName(@PathVariable("symbolic") String symbolic) {
        LOGGER.info("findBySymbolicName(symbolic= {})", symbolic);

        return crudService.findBySymbolicName(symbolic);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public SystemDTO update(HttpServletRequest request, @PathVariable("id") Long id, @RequestBody @Valid SystemDTO dto) {
        dto.setId(id);
        LOGGER.info("update(id={})", id);
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));

        return crudService.update(dto, request.getRemoteUser());
    }

    void setUser(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("httpservlet request from setUser {} ", request);
        if(request == null)
            crudService.setUser();
        else
            crudService.setUser(request.getRemoteUser());
    }


    private SystemDTO addDoc(SystemDTO dto, String user) {
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));
        return crudService.add(dto, user);
    }

    private void initFileParamsFromRequest(SystemDTO dto, MultipartFile mpf) throws Exception {
        dto.setFileLength((long) mpf.getBytes().length);
        dto.setFileMimeType(mpf.getContentType());
        dto.setFileName(mpf.getOriginalFilename());
    }

    private String writeContent(UUID uuid, byte[] bytes) throws Exception {
        return storageActionsService.writeFile(getRootName(), uuid, bytes);
    }


    private SystemDTO writeContent(SystemDTO dto, MultipartFile mpf, String user) throws Exception {
        try {
            if(!checkMultipartFile(mpf))
                throw new Exception("The multipart file contains either empty content type or empty filename or does not contain data");
            LOGGER.debug("the document: {} has been added", dto);
            LOGGER.debug("start adding file to FS");
            dto.setFilePath(writeContent(dto.getUuid(), mpf.getBytes()));
            SystemDTO updated = crudService.update(dto, user);
            LOGGER.debug("Dto object has been updated: {}", updated);
            return updated;
        }
        catch (Exception e) {

//            todo add custom Exception
            LOGGER.error("The exception has been occured while addContent method is executing {} {}", e.getMessage(), e);
            crudService.delete(dto.getId());
            throw new Exception("Error has been occured " + e.getMessage());
        }
    }

    private boolean checkMultipartFile(MultipartFile mpf ) throws IOException {
        return !StringUtils.isBlank(mpf.getOriginalFilename()) &&
                !StringUtils.isBlank(mpf.getContentType()) &&
                mpf.getBytes().length > 0;
    }

    private Storages getDefaultStorage() throws Exception {

        String currentStorageId = JsonNodeParser.getValueJsonNode(settingsNode, "currentStorageID");

        LOGGER.debug("getDefaultStorage(): currentStorageId: {} ", currentStorageId);
        if(StringUtils.isBlank(currentStorageId))
            throw new Exception("StorageId is not set up");

        Storages storages = Storages.getStorageByName(currentStorageId);
        LOGGER.debug("getDefaultStorage(): Storages: {} ", storages);
        return storages;
    }

    private String getRootName () throws Exception {
        Storages currentStorage = getDefaultStorage();

        return JsonNodeParser.getValueJsonNode(settingsNode, currentStorage.equals(Storages.AMAZONSTORAGE) ? "bucketName": "repository");
    }

}
