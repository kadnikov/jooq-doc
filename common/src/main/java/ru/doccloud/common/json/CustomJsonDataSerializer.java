package ru.doccloud.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrey Kadnikov
 */
public class CustomJsonDataSerializer extends JsonSerializer<JsonNode> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomJsonDataSerializer.class);

    public CustomJsonDataSerializer() {
    }

    @Override
    public void serialize(JsonNode node, JsonGenerator jsonGenerator, SerializerProvider provider)
            throws IOException {
        if (node == null) {
        	LOGGER.info("Node == null");
            jsonGenerator.writeNull();
        }
        else {
        	LOGGER.info("Node data : {}",node);
            jsonGenerator.writeString(node.toString());
        }
    }
}
