package ru.doccloud.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Andrey Kadnikov
 */
@Controller
public class FrontendLoaderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontendLoaderController.class);

    private static final String FRONTEND_APPLICATION_VIEW = "frontend/client";

    @RequestMapping(value = "/docs", method = RequestMethod.GET)
    public String startClient() {
        LOGGER.debug("Starting frontend single page application.");
        return FRONTEND_APPLICATION_VIEW;
    }
}
