package ru.doccloud.storagemanager;

public enum Storages {

    FILESTORAGE ("file_storage_area", "fs_storage"),
    AMAZONSTORAGE("amazon_storage_area", "bucketName");

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
