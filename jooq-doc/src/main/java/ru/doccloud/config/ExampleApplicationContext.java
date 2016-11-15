package ru.doccloud.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Andrey Kadnikov
 */
@Configuration
@ComponentScan({
        "ru.doccloud.document.service",
        "org.example.cmis.server"
})
@Import({
        PersistenceContext.class,
        WebAppContext.class
})
@ImportResource("classpath:org/jtransfo/spring/jTransfoContext.xml")
public class ExampleApplicationContext {
}