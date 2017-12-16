package ru.doccloud.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;

import java.util.List;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentRepository extends CommonRepository {

    /**
     * Adds a new Document.
     * @param documentEntry  The information of the added document entry.
     * @return  The added document entry.
     */
    public Document add(Document documentEntry);

    /**
     * Deletes a document entry.
     * @param id    The id of the deleted document entry.
     * @return  The deleted document entry.
     * @throws DocumentNotFoundException If the deleted todo entry is not found.
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
     * @throws DocumentNotFoundException If Document entry is not found.
     */
    public Document findById(Long id);

    /**
     * Finds a Document entry.
     * @param uuid    The uuid of the requested Document entry.
     * @return  The found Document entry.
     * @throws DocumentNotFoundException If Document entry is not found.
     */
    public Document findByUUID(String uuid);


    public Page<Document> findBySearchTerm(String searchTerm, Pageable pageable);

    /**
     * Updates the information of a Document entry.
     * @param todoEntry   The new information of a Document entry.
     * @return  The updated Document entry.
     * @throws DocumentNotFoundException If the updated Document entry is not found.
     */
    public Document update(Document todoEntry);

    public List<Document> findAllByParent(Long parent);

    public List<Document> findParents(Long docId);

    public Link addLink(Long headId, Long tailId);

    public Link deleteLink(Long headId, Long tailId);

    public Document updateFileInfo(Document documentEntry);

    public Page<Document> findAll(Pageable pageable, String query);

    public Page<Document> findAllByType(String type, String[] fields, Pageable pageable, String query, String userName);

    public Document setParent(Document documentEntry);

    public List<Document> findAllByLinkParent(Long parent);

    public Page<Document> findAllByParentAndType(Long parentid, String type, Pageable pageable);

}