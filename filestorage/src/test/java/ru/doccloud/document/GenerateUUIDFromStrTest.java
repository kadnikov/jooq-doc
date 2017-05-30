package ru.doccloud.document;

import org.junit.Test;

import java.util.UUID;

/**
 * Created by ilya on 3/23/17.
 */
public class GenerateUUIDFromStrTest {

//    @Test
    public void generateUUID(){
        final String uuidStr = "11bb5b0f-c3a4-4bf4-9fe2-9f550036425a";

        System.out.println("UUIDSTR " + uuidStr);

        System.out.println("generated uuid " + UUID.fromString(uuidStr));
    }
}
