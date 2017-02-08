package ru.doccloud.docs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Petri Kainulainen
 */
@Configuration
@ImportResource("classpath:org/jtransfo/spring/jTransfoContext.xml")
public class ServiceTestContext {
}
