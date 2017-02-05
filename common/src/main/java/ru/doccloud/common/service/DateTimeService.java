package ru.doccloud.common.service;

import java.sql.Timestamp;

import org.joda.time.LocalDateTime;

/**
 * @author Andrey Kadnikov
 */


public interface DateTimeService {
	
    public LocalDateTime getCurrentDateTime();
	
    public Timestamp getCurrentTimestamp();
}
