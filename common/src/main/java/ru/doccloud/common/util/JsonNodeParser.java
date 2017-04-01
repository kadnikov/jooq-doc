package ru.doccloud.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonNodeParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonNodeParser.class);


    public static String getValueJsonNode(final JsonNode settingsNode, final String paramName) throws Exception {
        if(StringUtils.isBlank(paramName))
            throw new Exception("paramName is empty");

        LOGGER.debug("settingsNode {}", settingsNode);
        JsonNode value = settingsNode.findValue(paramName);
        if(value == null)
            throw new Exception("value for key " + paramName + "was not found in json settings");

        String rootFolder = String.valueOf(value.asText());
        LOGGER.debug("repository for save file {}", rootFolder);
        return rootFolder;
    }
}
