package ru.doccloud.document.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.fasterxml.jackson.databind.JsonNode;
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
import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.service.DocumentCrudService;
import ru.doccloud.document.service.DocumentSearchService;
import ru.doccloud.document.service.FileActionsService;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/api/docs")
public class DocumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentCrudService crudService;

    private final DocumentSearchService searchService;

    private final FileActionsService fileActionsService;

    @Autowired
    public DocumentController(DocumentCrudService crudService, DocumentSearchService searchService, FileActionsService fileActionsService) {
        this.crudService = crudService;
        this.searchService = searchService;
        this.fileActionsService = fileActionsService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDTO add(HttpServletRequest request, @RequestBody @Valid DocumentDTO dto) {
        LOGGER.debug("Adding new Document entry with information: {}", dto);
        DocumentDTO added = addDoc(dto, request.getRemoteUser());
        LOGGER.debug("Added Document entry: {}", added);

        return added;
    }


    @RequestMapping(value="/addcontent",headers="content-type=multipart/*",method=RequestMethod.POST)
    public DocumentDTO addContent(MultipartHttpServletRequest request,  @RequestBody @Valid DocumentDTO dto) throws Exception {

        LOGGER.debug("start adding new Document to database: {} ", dto);
        Iterator<String> itr =  request.getFileNames();
        if(!itr.hasNext())
            return addDoc(dto, request.getRemoteUser());
        MultipartFile mpf = request.getFile(itr.next());
        initFileParamsFromRequest(dto, mpf);

        DocumentDTO added = addDoc(dto, request.getRemoteUser());

        LOGGER.info("DTO Obj after save: {}", added);
       return writeContent(added, mpf, request.getRemoteUser());
    }

    @RequestMapping(value="/createdoc",headers="content-type=multipart/*",method=RequestMethod.POST)
    public DocumentDTO addContent(MultipartHttpServletRequest request) throws Exception {
        LOGGER.info("add new document with content ");

        DocumentDTO dto = new DocumentDTO();

        Iterator<String> itr =  request.getFileNames();
        if(!itr.hasNext())
            return addDoc(dto, request.getRemoteUser());
        MultipartFile mpf = request.getFile(itr.next());
        initFileParamsFromRequest(dto, mpf);
        DocumentDTO added = addDoc(dto, request.getRemoteUser());
        LOGGER.debug("the document: {} has been added", added);

        return writeContent(added, mpf, request.getRemoteUser());
    }


    @RequestMapping(value="/updatecontent/{id}",headers="content-type=multipart/*",method=RequestMethod.PUT)
    public DocumentDTO updateContent(MultipartHttpServletRequest request,  @PathVariable("id") Long id) throws Exception {
        LOGGER.debug("find the document with id : {} ", id);

        DocumentDTO dto = crudService.findById(id);

        LOGGER.debug("Found Document entry: {}", dto);

        if(dto == null)
            throw new Exception("The document with such id " + id + " was not found in database ");

        Iterator<String> itr =  request.getFileNames();

        MultipartFile mpf = request.getFile(itr.next());

        return writeContent(dto, mpf, request.getRemoteUser());
    }

    @RequestMapping(value="/getcontent/{id}",headers="content-type=multipart/*",method=RequestMethod.GET)
    public byte[] getContent( @PathVariable("id") Long id) throws Exception {
        DocumentDTO dto = crudService.findById(id);

        LOGGER.debug("Found Document entry: {}", dto);

        if(dto == null)
            throw new Exception("The document with such id " + id + " was not found in database ");

        final String filePath = dto.getFilePath();
        if(StringUtils.isBlank(filePath)) {
            LOGGER.error("Filepath is empty. Content for document {} does not exist", dto);
            throw new Exception("Filepath is empty, conteny for document " + dto + "does not exist");
        }

        return fileActionsService.readFile(filePath);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public DocumentDTO delete(@PathVariable("id") Long id) {
        LOGGER.info("Deleting Document entry with id: {}", id);

        DocumentDTO deleted = crudService.delete(id);

        LOGGER.info("Deleted Document entry: {}", deleted);

        return deleted;
    }

    /**@RequestMapping(method = RequestMethod.GET)
    public List<DocumentDTO> findAll() {
        LOGGER.info("Finding all Document entries");

        List<DocumentDTO> documentEntries = crudService.findAll();

        LOGGER.info("Found {} Document entries.");

        return documentEntries;
    }*/
    
    @RequestMapping(method = RequestMethod.GET)
    public Page<DocumentDTO> findAll(Pageable pageable) {
        LOGGER.info("Finding {} Document entries for page {} ",
                pageable.getPageSize(),
                pageable.getPageNumber()
        );

        Page<DocumentDTO> docEntries = crudService.findAll(pageable);

        LOGGER.info("Found {} Document entries for page: {}",
        		docEntries.getNumberOfElements(),
        		docEntries.getNumber()
        );

        return docEntries;
    }
    
    @RequestMapping(value = "/type/{type}", method = RequestMethod.GET)
    public Page<DocumentDTO> findByType(@PathVariable("type") String type, @RequestParam(value = "fields",required=false) String fields, @RequestParam(value = "filters",required=false) String query,Pageable pageable) {
        LOGGER.info("Finding {} Document entries for page {} by type: {} and fields {}",
                pageable.getPageSize(),
                pageable.getPageNumber(),
                type, fields
        );
        String[] fieldsArr = null;
        if (fields!=null){
        	fieldsArr = fields.split(",");
        }

        Page<DocumentDTO> documentEntries = crudService.findAllByType(type, fieldsArr, pageable, query);

        LOGGER.info("Found {} Document entries for page: {}",
        		documentEntries.getNumberOfElements(),
        		documentEntries.getNumber()
        );

        return documentEntries;
    }

    @RequestMapping(value = "/parent/{parentid}", method = RequestMethod.GET)
    public List<DocumentDTO> findByParent(@PathVariable("parentid") Long parentid) {
        LOGGER.info("Finding all Documents by parent");

        List<DocumentDTO> documentEntries = crudService.findAllByParent(parentid);

        LOGGER.info("Found {} Document entries.");

        return documentEntries;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public DocumentDTO findById(@PathVariable("id") Long id) {
        LOGGER.info("Finding Document entry with id: {}", id);

        DocumentDTO found = crudService.findById(id);

        LOGGER.info("Found Document entry: {}", found);

        return found;
    }

    @RequestMapping(value = "/uuid/{uuid}", method = RequestMethod.GET)
    public DocumentDTO findByUUID(@PathVariable("uuid") String uuid) {
        LOGGER.info("Finding Document entry with id: {}", uuid);

        DocumentDTO found = crudService.findByUUID(uuid);

        LOGGER.info("Found Document entry: {}", found);

        return found;
    }


    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Page<DocumentDTO> findBySearchTerm(@RequestParam("searchTerm") String searchTerm, Pageable pageable) {
        LOGGER.info("Finding {} Document entries for page {} by using search term: {}",
                pageable.getPageSize(),
                pageable.getPageNumber(),
                searchTerm
        );

        Page<DocumentDTO> docEntries = searchService.findBySearchTerm(searchTerm, pageable);

        LOGGER.info("Found {} Document entries for page: {}",
        		docEntries.getNumberOfElements(),
        		docEntries.getNumber()
        );

        return docEntries;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public DocumentDTO update(HttpServletRequest request, @PathVariable("id") Long id, @RequestBody @Valid DocumentDTO dto) {
        dto.setId(id);
        LOGGER.info("Updating Document entry with information: {}", dto);
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));
        DocumentDTO updated = crudService.update(dto, request.getRemoteUser());
        LOGGER.info("Updated Document entry: {}", updated);

        return updated;
    }

    void setUser(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("httpservlet request from setUser {} ", request);
        if(request == null)
            crudService.setUser();
        else
            crudService.setUser(request.getRemoteUser());
    }


    private DocumentDTO addDoc(DocumentDTO dto, String user) {
        dto.setDocVersion(VersionHelper.generateMinorDocVersion(dto.getDocVersion()));
        return crudService.add(dto, user);
    }

    private void initFileParamsFromRequest(DocumentDTO dto, MultipartFile mpf) throws Exception {
        dto.setFileLength((long) mpf.getBytes().length);
        dto.setFileMimeType(mpf.getContentType());
        dto.setFileName(mpf.getOriginalFilename());
    }

    private String writeContent(UUID uuid, byte[] bytes) throws Exception {
        DocumentDTO settings = crudService.findSettings();
        JsonNode settingsNode = settings.getData();
        return fileActionsService.writeFile(JsonNodeParser.getValueJsonNode(settingsNode, "repository"), uuid, bytes);
    }


    private DocumentDTO writeContent(DocumentDTO dto, MultipartFile mpf, String user) throws Exception {
        try {
            if(!checkMultipartFile(mpf))
                throw new Exception("The multipart file contains either empty content type or empty filename or does not contain data");
            LOGGER.debug("the document: {} has been added", dto);
            LOGGER.debug("start adding file to FS");
            dto.setFilePath(writeContent(dto.getUuid(), mpf.getBytes()));
            DocumentDTO updated = crudService.update(dto, user);
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

}
