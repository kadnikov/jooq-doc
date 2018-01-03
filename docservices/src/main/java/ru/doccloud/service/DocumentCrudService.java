package ru.doccloud.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.LinkDTO;

import java.util.List;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentCrudService {

    @Transactional
    DocumentDTO add(final DocumentDTO dto, final String user);

    public DocumentDTO addToFolder(final DocumentDTO todo, final Long id, String userName);

    public DocumentDTO delete(final Long id);

    public List<DocumentDTO> findAll();

    List<DocumentDTO> findParents(Long docId);

    @Transactional(readOnly = true)
    List<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable);

    public DocumentDTO findById(final Long id);

    public DocumentDTO findByUUID(final String uuid);

    public DocumentDTO update(final DocumentDTO updated, final String user);
	
	public Page<DocumentDTO> findAllByParent(final Long parentid, Pageable pageable);

	public Page<DocumentDTO> findAll(Pageable pageable, String query);

    @Transactional
    DocumentDTO updateFileInfo(final DocumentDTO dto, String userName);

    @Transactional
    LinkDTO addLink(Long headId, Long tailId);

    @Transactional
    LinkDTO deleteLink(Long headId, Long tailId);

    @Transactional
    void setUser();

    @Transactional
    void setUser(String userName);

    public Page<DocumentDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query);

    public DocumentDTO setParent(DocumentDTO dto, String userName);

    public Page<DocumentDTO> findAllByParentAndType(Long parentid, String type, final Pageable pageable);

}
