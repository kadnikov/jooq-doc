package ru.doccloud.document.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.document.dto.SystemDTO;

import java.util.List;

/**
 * @author Andrey Kadnikov
 */
public interface SystemCrudService {

    @Transactional
    SystemDTO add(final SystemDTO dto, final String user);

//    public SystemDTO addToFolder(final SystemDTO todo, final Long id);

    public SystemDTO delete(final Long id);

    public List<SystemDTO> findAll();


    @Transactional(readOnly = true)
    List<SystemDTO> findBySearchTerm(String searchTerm, Pageable pageable);

    public SystemDTO findById(final Long id);

    public SystemDTO findByUUID(final String uuid);

    @Transactional(readOnly = true)
    SystemDTO findSettings();

    public SystemDTO update(final SystemDTO updated, final String user);
	

	public Page<SystemDTO> findAll(Pageable pageable, String query);

    @Transactional
    SystemDTO updateFileInfo(final SystemDTO dto);


    @Transactional
    void setUser();

    @Transactional
    void setUser(String userName);

    public Page<SystemDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query);

}
