package ru.doccloud.repository;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.doccloud.common.service.DateTimeService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ilya on 6/3/17.
 */
public abstract class AbstractJooqRepository {

    final DSLContext jooq;

    final DateTimeService dateTimeService;

    AbstractJooqRepository(DSLContext jooq, DateTimeService dateTimeService) {
        this.jooq = jooq;
        this.dateTimeService = dateTimeService;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJooqRepository.class);

    @Transactional
    public void setUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.trace("Current Remote User - ",request.getRemoteUser());
        jooq.execute("SET my.username = '"+request.getRemoteUser()+"'");

    }

    @Transactional
    public void setUser(String userName) {
        LOGGER.trace("Current User - {}",userName);
        jooq.execute("SET my.username = '"+userName+"'");

        //jooq.execute("SELECT current_setting('my.username') FROM documents LIMIT 1;");
    }
}
