package ru.doccloud.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


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

    public static ObjectNode buildObjectNode(Record queryResult, String[] fields){
        ObjectNode data = JsonNodeFactory.instance.objectNode();
        ObjectMapper mapper = new ObjectMapper();
        if (fields!=null){
            for (String field : fields) {
                if (queryResult.getValue(field)!=null){
                    try {
                        data.put(field,mapper.readTree(queryResult.getValue(field).toString()));
                    } catch (IllegalArgumentException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return data;
    }
}
