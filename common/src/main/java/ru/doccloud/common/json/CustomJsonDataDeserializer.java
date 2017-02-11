package ru.doccloud.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Andrey Kadnikov
 */
public class CustomJsonDataDeserializer extends JsonDeserializer<JsonNode> {

    
    public CustomJsonDataDeserializer() {
    }

    @Override
    public JsonNode deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    	ObjectMapper mapper = new ObjectMapper();
    	JsonNode actualObj = null;
		try {
			actualObj = mapper.readTree(jsonParser.getText());
		} catch (IOException e) {
			e.printStackTrace();
		}

        return actualObj;
    }
}
