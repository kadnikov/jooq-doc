package ru.doccloud.storagemanager;


public enum Storages {

//    DEFAULT("fileStorage", ""),
    FILESTORAGE ("fileStorage", "repository"),
    SCALITYAMAZONSTORAGE("scalityAmazonStorage", "bucketName"),
    AMAZONSTORAGE("amazonStorage", "bucketName");

    private String storageName;

    private String rootName;

    Storages(String storageName, String rootName) {
        this.storageName = storageName;
        this.rootName = rootName;
    }

    public String getStorageName() {
        return storageName;
    }

    public String getRootName() {
        return rootName;
    }

    public static Storages getStorageByName(final String storageName) {
        for (Storages storages : Storages.values()){
            if(storages.getStorageName().equals(storageName)) {
                return storages;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Storages{" +
                "storageName='" + storageName + '\'' +
                '}';
    }
}
