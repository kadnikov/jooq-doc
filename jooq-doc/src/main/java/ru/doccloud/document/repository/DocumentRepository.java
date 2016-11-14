package ru.doccloud.document.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ru.doccloud.document.model.Document;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentRepository {

    /**
     * Adds a new Document.
     * @param documentoEntry  The information of the added document entry.
     * @return  The added document entry.
     */
    public Document add(Document documentEntry);

    /**
     * Deletes a document entry.
     * @param id    The id of the deleted document entry.
     * @return  The deleted document entry.
     * @throws ru.doccloud.document.exception.DocumentNotFoundException If the deleted todo entry is not found.
     */
    public Document delete(Long id);

    /**
     * Finds all Document entries.
     * @return  Found Document entries.
     */
    public List<Document> findAll();

    /**
     * Finds a Document entry.
     * @param id    The id of the requested Document entry.
     * @return  The found Document entry.
     * @throws ru.doccloud.document.exception.DocumentNotFoundException If Document entry is not found.
     */
    public Document findById(Long id);

    public Page<Document> findBySearchTerm(String searchTerm, Pageable pageable);

    /**
     * Updates the information of a Document entry.
     * @param todoEntry   The new information of a Document entry.
     * @return  The updated Document entry.
     * @throws ru.doccloud.document.exception.DocumentNotFoundException If the updated Document entry is not found.
     */
    public Document update(Document todoEntry);

	public List<Document> findAllByType(String type);
	
	public List<Document> findAllByParent(Integer parent);
}
