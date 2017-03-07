package ru.doccloud.common.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyReader.class);
    public static String getProperty(final String propertyFileName, final String propertyName) {
        Properties prop = new Properties();
        try {
//            final String filename = "/props/config.properties";
            InputStream input = PropertyReader.class.getResourceAsStream(propertyFileName);
            if (input == null) {
                LOGGER.error("unable to find file: {}", propertyName);
                throw new FileNotFoundException("unable to find file " + propertyFileName);
            }
            prop.load(input);

            return prop.getProperty(propertyName);
        } catch (IOException ex) {
            LOGGER.error("The property with name: {}", propertyName);
            ex.printStackTrace();
            return null;
        }
    }
}
