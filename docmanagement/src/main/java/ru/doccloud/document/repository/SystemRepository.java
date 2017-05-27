package ru.doccloud.document.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.model.System;

import java.util.List;

/**
 * @author Andrey Kadnikov
 */
public interface SystemRepository {

    /**
     * Adds a new System.
     * @param systemEntry  The information of the added System entry.
     * @return  The added System entry.
     */
    public System add(System systemEntry);

    /**
     * Deletes a System entry.
     * @param id    The id of the deleted System entry.
     * @return  The deleted System entry.
     * @throws DocumentNotFoundException If the deleted  entry is not found.
     */
    public System delete(Long id);

    /**
     * Finds all System entries.
     * @return  Found System entries.
     */
    public List<System> findAll();

    /**
     * Finds a System entry.
     * @param id    The id of the requested System entry.
     * @return  The found System entry.
     * @throws DocumentNotFoundException If System entry is not found.
     */
    public System findById(Long id);

    /**
     * Finds a System entry.
     * @param uuid    The uuid of the requested System entry.
     * @return  The found System entry.
     * @throws DocumentNotFoundException If System entry is not found.
     */
    public System findByUUID(String uuid);


    @Transactional(readOnly = true)
    System findSettings();

    public Page<System> findBySearchTerm(String searchTerm, Pageable pageable);

    /**
     * Updates the information of a System entry.
     * @param todoEntry   The new information of a System entry.
     * @return  The updated System entry.
     * @throws DocumentNotFoundException If the updated System entry is not found.
     */
    public System update(System todoEntry);


	public System updateFileInfo(System systemEntry);

	public void setUser();

	public void setUser(String userName);

	public Page<System> findAll(Pageable pageable, String query);

	public Page<System> findAllByType(String type, String[] fields, Pageable pageable, String query);

}
