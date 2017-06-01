package ru.doccloud.document;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.doccloud.document.repository.AmazonReposiroryImpl;
import ru.doccloud.document.repository.AmazonRepository;
import ru.doccloud.document.repository.FileRepository;
import ru.doccloud.document.repository.FileRepositoryImpl;
import ru.doccloud.document.service.AmazonServiceImpl;
import ru.doccloud.document.service.FileActionsServiceImpl;
import ru.doccloud.storage.StorageActionsService;


@Component("storageManager")
@Service
public class StorageManagerImpl implements StorageManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageManagerImpl.class);

//    @Autowired
//    @Resource(name = "fileActionsService")
    private
    StorageActionsService fileActionsService;

//    @Autowired
//    @Resource(name = "amazonActionsService")
    private
    StorageActionsService amazonActionsService;


    public StorageActionsService getStorageService(Storages storage){
//        todo init via spring framework
        LOGGER.info("StorageManagerImpl(): amazonActionsService {}, fileActionsService {}", amazonActionsService, fileActionsService);
        amazonActionsService = amazonActionsService();
        fileActionsService = fileActionsService();
        return storage.equals(Storages.AMAZONSTORAGE) ? amazonActionsService : fileActionsService;
    }

    private FileRepository fileRepository(){
        return new FileRepositoryImpl();
    }

    private AmazonRepository amazonReposirory(){
        return new AmazonReposiroryImpl();
    }

    private StorageActionsService fileActionsService() {
        return new FileActionsServiceImpl(fileRepository());
    }

    private StorageActionsService amazonActionsService() {
        return new AmazonServiceImpl(amazonReposirory());
    }

}
