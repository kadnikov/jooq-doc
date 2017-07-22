package ru.doccloud.document.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.DocumentSearchService;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/restapi/systemdata")
public class IAController  extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IAController.class);

    private final DocumentCrudService crudService;

    private final DocumentSearchService searchService;

    @Autowired
    public IAController(DocumentCrudService crudService, DocumentSearchService searchService,
                              StorageAreaSettings storageAreaSettings, StorageManager storageManager) throws Exception {
        super(storageAreaSettings, storageManager);
        LOGGER.info("DocumentController(crudService={}, searchService = {}, storageAreaSettings= {}, storageManager={})", crudService, searchService, storageAreaSettings, storageManager);
        this.crudService = crudService;
        this.searchService = searchService;

        LOGGER.info("DocumentController(): storage settings {}", storageSettingsNode);

    }

    @RequestMapping(value = "/tenants", method = RequestMethod.GET)
    public List<DocumentDTO> findTenants() {
        LOGGER.info("findTenants");
        Long parentid = Long.parseLong("0");
        return crudService.findAllByParent(parentid);
    }
    
    @RequestMapping(value = "/tenants/{tenantId}/applications", method = RequestMethod.GET)
    public List<DocumentDTO> findAplications(@PathVariable("tenantId") String tenantId) {
        LOGGER.info("findApplications");
        Long parentid = Long.parseLong(tenantId);
        return crudService.findAllByParent(parentid);
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

    void setUser(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("httpservlet request from setUser {} ", request);
        if(request == null)
            crudService.setUser();
        else
            crudService.setUser(request.getRemoteUser());
    }

}
