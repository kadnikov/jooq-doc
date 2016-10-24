package ru.doccloud.document.service;

import java.util.List;

import ru.doccloud.document.dto.DocumentDTO;

/**
 * @author Andrey Kadnikov
 */
public interface DocumentCrudService {

    public DocumentDTO add(DocumentDTO todo);

    public DocumentDTO delete(Long id);

    public List<DocumentDTO> findAll();

    public DocumentDTO findById(Long id);

    public DocumentDTO update(DocumentDTO updated);
}
