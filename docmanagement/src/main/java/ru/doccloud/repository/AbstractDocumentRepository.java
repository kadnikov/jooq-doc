package ru.doccloud.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.AbstractDocument;
import ru.doccloud.document.model.Link;

import java.util.List;

/**
 * Created by ilya on 6/3/17.
 */
public interface AbstractDocumentRepository {

        /**
         * Adds a new Document.
         * @param documentEntry  The information of the added document entry.
         * @return  The added document entry.
         */
        public AbstractDocument add(AbstractDocument documentEntry);

        /**
         * Deletes a document entry.
         * @param id    The id of the deleted document entry.
         * @return  The deleted document entry.
         * @throws DocumentNotFoundException If the deleted todo entry is not found.
         */
        public AbstractDocument delete(Long id);

        /**
         * Finds all Document entries.
         * @return  Found Document entries.
         */
        public List<AbstractDocument> findAll();

        /**
         * Finds a Document entry.
         * @param id    The id of the requested Document entry.
         * @return  The found Document entry.
         * @throws DocumentNotFoundException If Document entry is not found.
         */
        public AbstractDocument findById(Long id);

        /**
         * Finds a Document entry.
         * @param uuid    The uuid of the requested Document entry.
         * @return  The found Document entry.
         * @throws DocumentNotFoundException If Document entry is not found.
         */
        public AbstractDocument findByUUID(String uuid);

        public Page<AbstractDocument> findBySearchTerm(String searchTerm, Pageable pageable);

        /**
         * Updates the information of a Document entry.
         * @param todoEntry   The new information of a Document entry.
         * @return  The updated Document entry.
         * @throws DocumentNotFoundException If the updated Document entry is not found.
         */
        public AbstractDocument update(AbstractDocument todoEntry);

        public List<AbstractDocument> findAllByParent(Long parent);

        public List<AbstractDocument> findParents(Long docId);

        public Link addLink(Long headId, Long tailId);

        public Link deleteLink(Long headId, Long tailId);

        public AbstractDocument updateFileInfo(AbstractDocument documentEntry);

        public void setUser();

        public void setUser(String userName);

        public Page<AbstractDocument> findAll(Pageable pageable, String query);

        public Page<AbstractDocument> findAllByType(String type, String[] fields, Pageable pageable, String query);

        public AbstractDocument setParent(AbstractDocument documentEntry);

        public List<AbstractDocument> findAllByLinkParent(Long parent);

}
