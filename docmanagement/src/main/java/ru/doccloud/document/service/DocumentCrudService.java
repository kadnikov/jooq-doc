package ru.doccloud.document.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentCrudService {

    @Transactional
    DocumentDTO add(final DocumentDTO dto, final String user);

    public DocumentDTO addToFolder(final DocumentDTO todo, final Long id);

    public DocumentDTO delete(final Long id);

    public List<DocumentDTO> findAll();

    List<DocumentDTO> findParents(Long docId);

    @Transactional(readOnly = true)
    List<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable);

    public DocumentDTO findById(final Long id);

    public DocumentDTO update(final DocumentDTO updated, final String user);
	
	public List<DocumentDTO> findAllByParent(final Long parentid);

	public Page<DocumentDTO> findAll(final Pageable pageable);


    @Transactional
    DocumentDTO updateFileInfo(final DocumentDTO dto);

    @Transactional
    Link addLink(Long headId, Long tailId);

    @Transactional
    Link deleteLink(Long headId, Long tailId);

    @Transactional
    void setUser();

    @Transactional
    void setUser(String userName);

    public Page<DocumentDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query);


}
