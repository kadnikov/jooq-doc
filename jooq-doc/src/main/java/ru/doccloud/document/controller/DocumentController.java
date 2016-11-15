package ru.doccloud.document.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.service.DocumentCrudService;
import ru.doccloud.document.service.DocumentSearchService;

/**
 * @author Andrey Kadnikov
 */
@RestController
@RequestMapping("/api/todo")
public class DocumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentCrudService crudService;

    private final DocumentSearchService searchService;

    @Autowired
    public DocumentController(DocumentCrudService crudService, DocumentSearchService searchService) {
        this.crudService = crudService;
        this.searchService = searchService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDTO add(@RequestBody @Valid DocumentDTO dto) {
        LOGGER.debug("Adding new Document entry with information: {}", dto);

        DocumentDTO added = crudService.add(dto);

        LOGGER.info("Added Document entry: {}", added);

        return added;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public DocumentDTO delete(@PathVariable("id") Long id) {
        LOGGER.info("Deleting Document entry with id: {}", id);

        DocumentDTO deleted = crudService.delete(id);

        LOGGER.info("Deleted Document entry: {}", deleted);

        return deleted;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<DocumentDTO> findAll() {
        LOGGER.info("Finding all Document entries");

        List<DocumentDTO> documentEntries = crudService.findAll();

        LOGGER.info("Found {} Document entries.");

        return documentEntries;
    }
    
    @RequestMapping(value = "/type/{type}", method = RequestMethod.GET)
    public List<DocumentDTO> findByType(@PathVariable("type") String type) {
        LOGGER.info("Finding all Documents by type");

        List<DocumentDTO> documentEntries = crudService.findAllByType(type);

        LOGGER.info("Found {} Document entries.");

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
    public DocumentDTO update(@PathVariable("id") Long id, @RequestBody @Valid DocumentDTO dto) {
        dto.setId(id);

        LOGGER.info("Updating Document entry with information: {}", dto);

        DocumentDTO updated = crudService.update(dto);

        LOGGER.info("Updated Document entry: {}", updated);

        return updated;
    }
}
