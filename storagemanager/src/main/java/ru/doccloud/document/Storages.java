package ru.doccloud.document;


public enum Storages {

    FILESTORAGE ("fileStorage"),
    AMAZONSTORAGE("amazonStorage");

    private String storageName;

    Storages(String storageName) {
        this.storageName = storageName;
    }

    public String getStorageName() {
        return storageName;
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
