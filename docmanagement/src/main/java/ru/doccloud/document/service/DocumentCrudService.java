package ru.doccloud.document.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ru.doccloud.document.dto.DocumentDTO;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentCrudService {

    public DocumentDTO add(DocumentDTO todo);
    
    public DocumentDTO addToFolder(DocumentDTO todo, Long id);

    public DocumentDTO delete(Long id);

    public List<DocumentDTO> findAll();

    public DocumentDTO findById(Long id);

    public DocumentDTO update(DocumentDTO updated);
	
	public List<DocumentDTO> findAllByParent(Long parentid);

	public Page<DocumentDTO> findAll(Pageable pageable);

	public Page<DocumentDTO> findAllByType(String type, String[] fields, Pageable pageable, String query);
}
