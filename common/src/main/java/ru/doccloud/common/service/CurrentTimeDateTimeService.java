package ru.doccloud.common.service;

import java.sql.Timestamp;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Andrey Kadnikov
 */
@Component("currentTimeDateTimeService")
public class CurrentTimeDateTimeService implements DateTimeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrentTimeDateTimeService.class);


	@Override
    public LocalDateTime getCurrentDateTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        LOGGER.debug("Returning current datetime: {}", currentTime);

        return currentTime;
    }

    @Override
    public Timestamp getCurrentTimestamp() {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        LOGGER.debug("Returning current timestamp: {}", currentTime);

        return currentTime;
    }
}
