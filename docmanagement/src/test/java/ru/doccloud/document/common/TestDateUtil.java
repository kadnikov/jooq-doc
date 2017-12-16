package ru.doccloud.document.common;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Timestamp;

public class TestDateUtil {

    public static final String CURRENT_TIMESTAMP = "2014-02-18 11:13:28";
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static LocalDateTime parseLocalDateTime(String timestamp) {
        DateTimeFormatter format = DateTimeFormat.forPattern(TIMESTAMP_PATTERN);
        return format.parseLocalDateTime(timestamp);
    }

    public static Timestamp parseTimestamp(String timestamp) {
        return Timestamp.valueOf(timestamp);
    }
}
