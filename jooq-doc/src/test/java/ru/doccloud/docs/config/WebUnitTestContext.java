package ru.doccloud.docs.config;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ru.doccloud.config.WebAppContext;
import ru.doccloud.document.service.DocumentCrudService;
import ru.doccloud.document.service.DocumentSearchService;

/**
 * @author Petri Kainulainen
 */
@Configuration
@Import({WebAppContext.class})
public class WebUnitTestContext {

    @Bean
    public DocumentCrudService todoCrudService() {
        return mock(DocumentCrudService.class);
    }

    @Bean
    public DocumentSearchService todoSearchService() {
        return mock(DocumentSearchService.class);
    }
}
