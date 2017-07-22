package ru.doccloud.common.global;

/**
 * Created by ilya on 7/22/17.
 */
public enum SettingsKeys {
     STORAGE_AREA_KEY("storage_area"),
     CMIS_SETTINGS_KEY("cmis_settings");

     private String settingsKey;

    SettingsKeys(String settingsKey) {
        this.settingsKey = settingsKey;
    }

    public String getSettingsKey() {
        return settingsKey;
    }
}
