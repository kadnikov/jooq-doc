package ru.doccloud.docs.common.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.doccloud.common.service.DateTimeService;
import ru.doccloud.docs.common.TestDateUtil;

import java.sql.Timestamp;

/**
 */
@Profile("test")
@Component
public class ConstantTimeDateTimeService implements DateTimeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantTimeDateTimeService.class);

    @Override
    public LocalDateTime getCurrentDateTime() {
        LOGGER.debug("Returning constant datetime: {}", TestDateUtil.CURRENT_TIMESTAMP);
        return TestDateUtil.parseLocalDateTime(TestDateUtil.CURRENT_TIMESTAMP);
    }

    @Override
    public Timestamp getCurrentTimestamp() {
        LOGGER.debug("Returning constant timestamp: {}", TestDateUtil.CURRENT_TIMESTAMP);
        return Timestamp.valueOf(TestDateUtil.CURRENT_TIMESTAMP);
    }
}
