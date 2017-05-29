package ru.doccloud.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.tools.json.JSONParser;
import org.junit.Test;
import ru.doccloud.common.util.JsonNodeParser;

import java.io.IOException;

/**
 * Created by ilya on 5/28/17.
 */
public class JsonParseTest {
    private static final String JSON = "{\n" +
            "    \"supportedStorages\":[{\n" +
            "            \"fileStorage\":[{\n" +
            "                \"maxsize\":\"1024\",\n" +
            "                \"repository\":\"/home/ilya/filenet_workspace/testwritefile\"\n" +
            "            }],\n" +
            "            \"amazonStorage\":[{\n" +
            "                \"awsAccessKey\":\"AKIAJWB5GOLHL3TLCVMA\",\n" +
            "                \"awsSecretAccessKey\":\"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\",\n" +
            "                \"awsRegion\":\"doccloud\",\n" +
            "                \"bucketName\":\"doccloud\"\n" +
            "\n" +
            "            }],\n" +
            "            \"currentStorage\":[{\n" +
            "                \"currentStorageID\":\"fileStorage\"\n" +
            "            }]\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    @Test
    public void jsonParse(){
        JSONParser parser = new JSONParser();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(JSON);
            JsonNodeParser.getValueJsonNode(actualObj, "bucketName");
//            JSONObject json = (JSONObject) parser.parse(JSON);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println(level);
    }
}
