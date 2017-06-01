package ru.doccloud.docs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.doccloud.config.WebAppContext;
import ru.doccloud.amazon.service.DocumentCrudService;
import ru.doccloud.amazon.service.DocumentSearchService;

import static org.mockito.Mockito.mock;

/**
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
