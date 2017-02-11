package ru.doccloud.common.json;

import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.LocalDateTime;

/**
 *
 */
public class JodaModule extends SimpleModule {

    public JodaModule() {
        super(PackageVersion.VERSION);

        addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
    }
}
