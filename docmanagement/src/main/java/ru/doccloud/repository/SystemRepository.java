package ru.doccloud.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.SystemDocument;

import java.util.List;

/**
 * @author Andrey Kadnikov
 */
public interface SystemRepository extends CommonRepository {

    /**
     * Adds a new SystemDocument.
     * @param systemEntry  The information of the added SystemDocument entry.
     * @return  The added SystemDocument entry.
     */
    public SystemDocument add(SystemDocument systemEntry);

    /**
     * Deletes a SystemDocument entry.
     * @param id    The id of the deleted SystemDocument entry.
     * @return  The deleted SystemDocument entry.
     * @throws DocumentNotFoundException If the deleted  entry is not found.
     */
    public SystemDocument delete(Long id);

    /**
     * Finds all SystemDocument entries.
     * @return  Found SystemDocument entries.
     */
    public List<SystemDocument> findAll();

    /**
     * Finds a SystemDocument entry.
     * @param id    The id of the requested SystemDocument entry.
     * @return  The found SystemDocument entry.
     * @throws DocumentNotFoundException If SystemDocument entry is not found.
     */
    public SystemDocument findById(Long id);

    /**
     * Finds a SystemDocument entry.
     * @param uuid    The uuid of the requested SystemDocument entry.
     * @return  The found SystemDocument entry.
     * @throws DocumentNotFoundException If SystemDocument entry is not found.
     */
    public SystemDocument findByUUID(String uuid);


    @Transactional(readOnly = true)
    SystemDocument findSettings(final String settingsKey);

    public Page<SystemDocument> findBySearchTerm(String searchTerm, Pageable pageable);

    /**
     * Updates the information of a SystemDocument entry.
     * @param todoEntry   The new information of a SystemDocument entry.
     * @return  The updated SystemDocument entry.
     * @throws DocumentNotFoundException If the updated SystemDocument entry is not found.
     */
    public SystemDocument update(SystemDocument todoEntry);


	public SystemDocument updateFileInfo(SystemDocument systemEntry);

	public Page<SystemDocument> findAll(Pageable pageable, String query);

	public Page<SystemDocument> findAllByType(String type, String[] fields, Pageable pageable, String query);

	public SystemDocument findBySymbolicName(String symbolic);

}
