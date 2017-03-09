package ru.doccloud.document.mockdocumentcontroller;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.*;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.junit.Before;
import org.junit.Test;
import ru.doccloud.document.mockdocumentcontroller.util.AbderaHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

public class TestDocumentController {

    private Abdera abdera;
    private Parser parser;
    private AbderaClient abderaClient;

    private final String filePath = "/home/ilya/filenet_workspace/tasks.txt";

    @Before
    public void init() throws URISyntaxException {
        abdera = new Abdera();
        abderaClient = AbderaHelper.getInstance().getAbderaClient(abdera);
        parser = abdera.getParser();
    }

    @Test
    public void addContentWithoutDocumentTest(){
        final RequestOptions options = abderaClient.getDefaultRequestOptions();
        options.setHeader("Content-Type", "multipart/related;type=\"application/atom+xml\"");

        StringPart entryPart = new StringPart("entry", "docName");
//        entryPart.setContentType("application/atom+xml");

        FilePart filePart = null;

        ClientResponse resp = null;

        String uri = "http://localhost/api/docs/createdoc";
        try {
            filePart = new FilePart("file", new File(filePath));
            RequestEntity request = new MultipartRequestEntity(new Part[] { entryPart, filePart}, abderaClient.getHttpClientParams());



            resp = abderaClient.post(uri, request, options);

            switch(resp.getType()) {
                case SUCCESS:
                    System.out.println("the forum was updated at: " + resp.getLocation());
                    break;
                default:
                    System.out.println("Error: " + resp.getStatusText());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
}
