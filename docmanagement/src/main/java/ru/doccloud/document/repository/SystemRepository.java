package ru.doccloud.document.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.SystemEntity;

import java.util.List;

/**
 * @author Andrey Kadnikov
 */
public interface SystemRepository {

    /**
     * Adds a new SystemEntity.
     * @param systemEntry  The information of the added SystemEntity entry.
     * @return  The added SystemEntity entry.
     */
    public SystemEntity add(SystemEntity systemEntry);

    /**
     * Deletes a SystemEntity entry.
     * @param id    The id of the deleted SystemEntity entry.
     * @return  The deleted SystemEntity entry.
     * @throws DocumentNotFoundException If the deleted  entry is not found.
     */
    public SystemEntity delete(Long id);

    /**
     * Finds all SystemEntity entries.
     * @return  Found SystemEntity entries.
     */
    public List<SystemEntity> findAll();

    /**
     * Finds a SystemEntity entry.
     * @param id    The id of the requested SystemEntity entry.
     * @return  The found SystemEntity entry.
     * @throws DocumentNotFoundException If SystemEntity entry is not found.
     */
    public SystemEntity findById(Long id);

    /**
     * Finds a SystemEntity entry.
     * @param uuid    The uuid of the requested SystemEntity entry.
     * @return  The found SystemEntity entry.
     * @throws DocumentNotFoundException If SystemEntity entry is not found.
     */
    public SystemEntity findByUUID(String uuid);


    @Transactional(readOnly = true)
    SystemEntity findSettings();

    public Page<SystemEntity> findBySearchTerm(String searchTerm, Pageable pageable);

    /**
     * Updates the information of a SystemEntity entry.
     * @param todoEntry   The new information of a SystemEntity entry.
     * @return  The updated SystemEntity entry.
     * @throws DocumentNotFoundException If the updated SystemEntity entry is not found.
     */
    public SystemEntity update(SystemEntity todoEntry);


	public SystemEntity updateFileInfo(SystemEntity systemEntry);

	public void setUser();

	public void setUser(String userName);

	public Page<SystemEntity> findAll(Pageable pageable, String query);

	public Page<SystemEntity> findAllByType(String type, String[] fields, Pageable pageable, String query);

	public SystemEntity findBySymbolicName(String symbolic);

}
