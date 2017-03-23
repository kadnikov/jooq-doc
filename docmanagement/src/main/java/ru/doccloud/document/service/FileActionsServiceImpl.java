package ru.doccloud.document.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.doccloud.document.repository.FileRepository;

import java.util.UUID;

@Service
public class FileActionsServiceImpl implements FileActionsService {

    private final FileRepository fileRepository;

    @Autowired
    public FileActionsServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public String writeFile(final UUID uuid,  final byte[] fileArr) throws Exception {
        return fileRepository.writeFile(uuid, fileArr);
    }

    @Override
    public byte[] readFile(final String filePath) throws Exception {
        return fileRepository.readFile(filePath);
    }

}

