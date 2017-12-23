package ru.doccloud.document.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.common.util.VersionHelper;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.DocumentSearchService;
import ru.doccloud.service.FileService;
import ru.doccloud.service.document.dto.DocumentDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Iterator;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/api/docs")
public class DocumentController  extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentCrudService crudService;

    private final DocumentSearchService searchService;


    @Autowired
    public DocumentController(DocumentCrudService crudService, DocumentSearchService searchService,
                              FileService fileService) throws Exception {
        super(fileService, crudService);
        LOGGER.trace("DocumentController(crudService={}, searchService = {}, fileService={})", crudService, searchService, fileService);
        this.crudService = crudService;
        this.searchService = searchService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDTO add(HttpServletRequest request, @RequestBody @Valid DocumentDTO dto) {
        LOGGER.debug("entering add(dto={}, requestUri={})",
                dto, request.getRequestURI());
        return addDoc(dto, request.getRemoteUser());
    }

    @RequestMapping(value="/addcontent",headers="content-type=multipart/*",method=RequestMethod.POST)
    public DocumentDTO addContent(MultipartHttpServletRequest request) throws Exception {

        LOGGER.info("addContent(): add new document from request uri {}", request.getRequestURI());
        DocumentDTO dto = new DocumentDTO();
        Iterator<String> itr =  request.getFileNames();
        if(!itr.hasNext())
            throw new Exception("Request does not contain any files");
        final MultipartFile mpf = request.getFile(itr.next());

        populateFilePartDto(dto, request, mpf);

        return writeContent(addDoc(dto, request.getRemoteUser()), mpf, request.getRemoteUser());
    }

    @RequestMapping(value="/updatecontent/{id}",headers="content-type=multipart/*",method=RequestMethod.POST)
    public DocumentDTO updateContent(MultipartHttpServletRequest request,  @PathVariable("id") Long id) throws Exception {
        LOGGER.info("entering updateContent(requestURI={}, id={})", id, request.getRequestURI());

        final DocumentDTO dto = crudService.findById(id);

        LOGGER.debug("updateContent(): dto = {}", dto);

        if(dto == null)
            throw new Exception("The document with such id " + id + " was not found in database ");

        Iterator<String> itr =  request.getFileNames();

        MultipartFile mpf = request.getFile(itr.next());
        populateFilePartDto(dto, request, mpf);
        return writeContent(dto, mpf, request.getRemoteUser());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public DocumentDTO update(HttpServletRequest request, @PathVariable("id") Long id, @RequestBody @Valid DocumentDTO dto) {
        dto.setId(id);
        LOGGER.info("update(id={})", id);
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));

        return crudService.update(dto, request.getRemoteUser());
    }

    @RequestMapping(value = "/updatefileinfo/{id}", method = RequestMethod.PUT)
    public DocumentDTO updateFileInfo(HttpServletRequest request, @PathVariable("id") Long id, @RequestBody @Valid DocumentDTO dto) {
        dto.setId(id);
        LOGGER.info("update file info (id={})", id);
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));

        return crudService.updateFileInfo(dto);
    }
    
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public DocumentDTO delete(@PathVariable("id") Long id) {
        LOGGER.info("delete(id={})", id);

        return crudService.delete(id);
    }

    @RequestMapping(value="/getcontent/{id}",method=RequestMethod.GET)
    public byte[] getContent( @PathVariable("id") Long id) throws Exception {

        LOGGER.info("getContent (id = {})", id);
        final DocumentDTO dto = crudService.findById(id);

        LOGGER.info("getContent(): Found Document {}", dto);

        if(dto == null)
            throw new Exception("The document with such id " + id + " was not found in database ");

        final String filePath = dto.getFilePath();
        if(StringUtils.isBlank(filePath)) {
            LOGGER.error("Filepath is empty. Content for document {} does not exist", dto);
            throw new Exception("Filepath is empty, content for document " + dto + "does not exist");
        }

        final JsonNode storageSettings = fileService.getStorageSettingByStorageAreaName(dto.getFileStorage());
        LOGGER.debug("getContent(): storageSettings {}", storageSettings);

        return fileService.readFile(storageSettings, filePath);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Page<DocumentDTO> findAll(Pageable pageable, @RequestParam(value = "filters",required=false) String query) {
        LOGGER.info("findAll(pageSize= {}, pageNumber = {}) ",
                pageable.getPageSize(),
                pageable.getPageNumber()
        );

        return crudService.findAll(pageable, query);
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
    public Page<DocumentDTO> findByParent(@PathVariable("parentid") Long parentid, Pageable pageable) {
        LOGGER.info("findByParent(parentid = {})", parentid);

        return crudService.findAllByParent(parentid, pageable);
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


    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Page<DocumentDTO> findBySearchTerm(@RequestParam("searchTerm") String searchTerm, Pageable pageable) {
        LOGGER.info("findBySearchTerm(searchTerm = {}, pageSize= {}, pageNumber = {}) ",
                searchTerm,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );

        return searchService.findBySearchTerm(searchTerm, pageable);
    }

    private DocumentDTO addDoc(DocumentDTO dto, String user) {

        LOGGER.debug("entering addDoc(dto={}, user= {}");
        final String minorDocVersion = VersionHelper.generateMinorDocVersion(dto.getDocVersion());
        LOGGER.debug("addDoc(): minorDocVersion {}", minorDocVersion);
        dto.setDocVersion(minorDocVersion);
        final DocumentDTO documentDTO =  crudService.add(dto, user);

        LOGGER.debug("leaving addDoc(): created document = {}", documentDTO);

        return documentDTO;
    }

    private DocumentDTO writeContent(DocumentDTO dto, MultipartFile mpf, String user) throws Exception {
        LOGGER.debug("entering writeContent(dto={}, user={})", dto, user);
        try {
            if(!checkMultipartFile(mpf))
                throw new Exception("The multipart file contains either empty content type or empty filename or does not contain data");
            LOGGER.debug("writeContent(): the document: {} has been added, starting write to storage", dto);

            final JsonNode storageSettings = fileService.getStorageSettingsByDocType(dto.getType());
            LOGGER.debug("writeContent(): storageSettings {}", storageSettings);
            final String filePath = fileService.writeContent(dto.getUuid(), mpf.getBytes(), storageSettings);
            LOGGER.debug("writeContent(): file has been saved, filePath {}", filePath);
            dto.setFilePath(filePath);
            dto.setFileStorage(JsonNodeParser.getStorageAreaName(storageSettings));
            DocumentDTO updated = crudService.updateFileInfo(dto);
            LOGGER.debug("leaving writeContent(): Dto object has been updated: {}", updated);
            return updated;
        }
        catch (Exception e) {

//            todo add custom Exception
            LOGGER.error("The exception has been occurred while addContent method is executing {} {}", e.getMessage(), e);
            crudService.delete(dto.getId());
            throw new Exception("Error has been occurred " + e.getMessage());
        }
    }

}
