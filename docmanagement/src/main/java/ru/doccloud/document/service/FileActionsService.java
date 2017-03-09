package ru.doccloud.document.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.doccloud.document.dto.DocumentDTO;

import java.util.List;

/**
 * @author Ilya Ushakov
 */

public interface FileActionsService {

    public String writeFile(final String fileName, final Long docId, final String docVersion, final byte[] fileArr) throws Exception;

    public byte[] readFile(final String filePath) throws Exception;

}