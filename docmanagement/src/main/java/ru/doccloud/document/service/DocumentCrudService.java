package ru.doccloud.document.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.model.Link;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentCrudService {

    public DocumentDTO add(final DocumentDTO todo);

    @Transactional
    DocumentDTO add(DocumentDTO dto, String user);

    public DocumentDTO addToFolder(final DocumentDTO todo, Long id);

    public DocumentDTO delete(final Long id);

    public List<DocumentDTO> findAll();

    List<DocumentDTO> findParents(Long docId);

    @Transactional(readOnly = true)
    List<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable);

    public DocumentDTO findById(final Long id);

    public DocumentDTO update(final DocumentDTO updated);
	
	public List<DocumentDTO> findAllByParent(final Long parentid);

	public Page<DocumentDTO> findAll(final Pageable pageable);

    @Transactional
    DocumentDTO update(DocumentDTO dto, String user);

    @Transactional
    DocumentDTO updateFileInfo(DocumentDTO dto);

    @Transactional
    Link addLink(Long headId, Long tailId);

    @Transactional
    Link deleteLink(Long headId, Long tailId);

    //todo remove this method
    @Transactional
    void setUser();

    @Transactional
    void setUser(String userName);

    public Page<DocumentDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query);
}
