package ru.doccloud.document.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ru.doccloud.document.dto.DocumentDTO;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentCrudService {

    public DocumentDTO add(final DocumentDTO todo);
    
    public DocumentDTO addToFolder(final DocumentDTO todo, Long id);

    public DocumentDTO delete(final Long id);

    public List<DocumentDTO> findAll();

    public DocumentDTO findById(final Long id);

    public DocumentDTO update(final DocumentDTO updated);
	
	public List<DocumentDTO> findAllByParent(final Long parentid);

	public Page<DocumentDTO> findAll(final Pageable pageable);

	public Page<DocumentDTO> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query);
}
