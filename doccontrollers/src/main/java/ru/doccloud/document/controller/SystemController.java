package ru.doccloud.document.controller;

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
import ru.doccloud.common.util.VersionHelper;
import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Iterator;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/api/system")
public class SystemController  extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemController.class);

    private final SystemCrudService crudService;

    @Autowired
    public SystemController(SystemCrudService crudService, StorageAreaSettings storageAreaSettings, StorageManager storageManager) throws Exception {
        super(storageAreaSettings, storageManager);
        LOGGER.info("SystemController(crudService={}, storageAreaSettings= {}, storageManager={})", crudService, storageAreaSettings, storageManager);
        this.crudService = crudService;
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
        SystemDTO dto = new SystemDTO();
        Iterator<String> itr =  request.getFileNames();
        if(!itr.hasNext())
            return addDoc(dto, request.getRemoteUser());
        final MultipartFile mpf = request.getFile(itr.next());

        populateFilePartDto(dto, request, mpf);

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


    private SystemDTO writeContent(SystemDTO dto, MultipartFile mpf, String user) throws Exception {
        try {
            if(!checkMultipartFile(mpf))
                throw new Exception("The multipart file contains either empty content type or empty filename or does not contain data");
            LOGGER.debug("the document: {} has been added", dto);
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



}
