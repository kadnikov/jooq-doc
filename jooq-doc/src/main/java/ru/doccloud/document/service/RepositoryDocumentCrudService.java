package ru.doccloud.document.service;

import java.util.List;

import org.jtransfo.JTransfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.repository.DocumentRepository;

/**
 * @author Andrey Kadnikov
 */
@Service
public class RepositoryDocumentCrudService implements DocumentCrudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDocumentCrudService.class);

    private final DocumentRepository repository;

    private final JTransfo transformer;

    @Autowired
    public RepositoryDocumentCrudService(DocumentRepository repository, JTransfo transformer) {
        this.repository = repository;
        this.transformer = transformer;
    }

    @Transactional
    @Override
    public DocumentDTO add(DocumentDTO dto) {
        LOGGER.info("Adding Document entry with information: {}", dto);
        if (dto.getId()==null){
        	//dto.setId(DEFAULT);
        }
        Document added = createModel(dto);
        Document persisted = repository.add(added);

        LOGGER.info("Added Document entry with information: {}", persisted);

        return transformer.convert(persisted, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO delete(Long id) {
        LOGGER.info("Deleting Document entry with id: {}", id);

        Document deleted = repository.delete(id);

        LOGGER.info("Deleted Document entry with id: {}", id);

        return transformer.convert(deleted, new DocumentDTO());
    }

    @Transactional(readOnly = true)
    @Override
    public List<DocumentDTO> findAll() {
        LOGGER.info("Finding all Document entries.");

        List<Document> docEntries = repository.findAll();

        LOGGER.debug("Found {} Document entries.", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
    }

    @Transactional(readOnly = true)
    @Override
    public DocumentDTO findById(Long id) {
        LOGGER.info("Finding Document entry with id: {}", id);

        Document found = repository.findById(id);

        LOGGER.info("Found Document entry: {}", found);

        return transformer.convert(found, new DocumentDTO());
    }

    @Transactional
    @Override
    public DocumentDTO update(DocumentDTO dto) {
        LOGGER.info("Updating the information of a Document entry: {}", dto);

        Document newInformation = createModel(dto);
        Document updated = repository.update(newInformation);

        LOGGER.debug("Updated the information of a Document entry: {}", updated);

        return transformer.convert(updated, new DocumentDTO());
    }

    private Document createModel(DocumentDTO dto) {
        return Document.getBuilder(dto.getTitle())
                .description(dto.getDescription())
                .type(dto.getType())
                .data(dto.getData())
                .id(dto.getId())
                .build();
    }

	@Override
	public List<DocumentDTO> findAllByType(String type) {
		LOGGER.info("Finding Documents by Type.");

        List<Document> docEntries = repository.findAllByType(type);

        LOGGER.debug("Found {} Document entries.", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
	}

	@Override
	public List<DocumentDTO> findAllByParent(Integer parentid) {
		LOGGER.info("Finding Documents by Type.");

        List<Document> docEntries = repository.findAllByParent(parentid);

        LOGGER.debug("Found {} Document entries.", docEntries.size());

        return transformer.convertList(docEntries, DocumentDTO.class);
	}
}
