package ru.doccloud.document.mockdocumentcontroller;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.protocol.client.AbderaClient;
import org.junit.Before;

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

//    @Test
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

    /**
     * Created by USHAK_I1 on 20.10.2014.
     */
    public static class AbderaHelper {

        private static final String SERVER_URI="http://localhost";
        private static final String SAFETY_SERVER_URI="https://localhost";

        private static final String USER_NAME = "test";
        private static final String USER_PWD = "test";

        private static AbderaHelper ourInstance;

        private Abdera abdera;
        private AbderaClient abderaClient;

        public static AbderaHelper getInstance() {
            if(ourInstance == null)
                ourInstance = new AbderaHelper();
            return ourInstance;
        }

        private AbderaHelper() {
            abdera = new Abdera();
        }

        public AbderaClient getAbderaClient() throws URISyntaxException {
            if(abdera == null)
                abdera = new Abdera();
            AbderaClient client = new AbderaClient(abdera);
            RequestOptions requestOptions = client.getDefaultRequestOptions();
            requestOptions.setUseChunked(false);
            requestOptions.setContentType("application/atom+xml");

            AbderaClient.registerTrustManager();
            client.addCredentials(SAFETY_SERVER_URI, null, null,
                    new UsernamePasswordCredentials(USER_NAME,
                            USER_PWD));
            client.usePreemptiveAuthentication(true);

            return client;
        }

        public AbderaClient getJSONAbderaClient() throws URISyntaxException {
            if(abdera == null)
                abdera = new Abdera();
            AbderaClient client = new AbderaClient(abdera);
            RequestOptions requestOptions = client.getDefaultRequestOptions();
            requestOptions.setUseChunked(false);
            requestOptions.setContentType("application/json; charset=UTF-8");

            AbderaClient.registerTrustManager();
            client.addCredentials(SAFETY_SERVER_URI, null, null,
                    new UsernamePasswordCredentials(USER_NAME,
                            USER_PWD));
            client.usePreemptiveAuthentication(true);

            return client;
        }

        public AbderaClient getAbderaClient(Abdera abdera) throws URISyntaxException {
            if(abdera == null)
                abdera = new Abdera();
            AbderaClient client = new AbderaClient(abdera);
            RequestOptions requestOptions = client.getDefaultRequestOptions();
            requestOptions.setUseChunked(false);
            requestOptions.setContentType("application/atom+xml");

            AbderaClient.registerTrustManager();
            client.addCredentials(SAFETY_SERVER_URI, null, null,
                    new UsernamePasswordCredentials(USER_NAME,
                            USER_PWD));
            client.usePreemptiveAuthentication(true);

            return client;
        }

        public AbderaClient getAbderaClient(Abdera abdera, final String userId, final String pwd) throws URISyntaxException {
            if(abdera == null)
                abdera = new Abdera();
            AbderaClient client = new AbderaClient(abdera);
            RequestOptions requestOptions = client.getDefaultRequestOptions();
            requestOptions.setUseChunked(false);
            requestOptions.setContentType("application/atom+xml");

            AbderaClient.registerTrustManager();
            client.addCredentials(SAFETY_SERVER_URI, null, null,
                    new UsernamePasswordCredentials(userId,
                            pwd));
            client.usePreemptiveAuthentication(true);

            return client;
        }

        public AbderaClient getAbderaClient(final String userId, final String pwd) throws URISyntaxException {
            AbderaClient client = new AbderaClient(new Abdera());
            RequestOptions requestOptions = client.getDefaultRequestOptions();
            requestOptions.setUseChunked(false);
            requestOptions.setContentType("application/atom+xml");

            AbderaClient.registerTrustManager();
            client.addCredentials(SAFETY_SERVER_URI, null, null,
                    new UsernamePasswordCredentials(userId,
                            pwd));
            client.usePreemptiveAuthentication(true);

            return client;
        }

        public AbderaClient getAbderaClient(Abdera abdera, final String safetyServerUrl, final String userId, final String pwd) throws URISyntaxException {
            if(abdera == null)
                abdera = new Abdera();
            AbderaClient client = new AbderaClient(abdera);
            RequestOptions requestOptions = client.getDefaultRequestOptions();
            requestOptions.setUseChunked(false);
            requestOptions.setContentType("application/atom+xml");

            AbderaClient.registerTrustManager();
            client.addCredentials(safetyServerUrl, null, null,
                    new UsernamePasswordCredentials(userId,
                            pwd));
            client.usePreemptiveAuthentication(true);

            return client;
        }

        public Abdera getAbdera(){
            if(abdera == null)
                abdera = new Abdera();
            return new Abdera();
        }

        public Parser getParser() {
            if(abdera == null)
                abdera = new Abdera();
            return abdera.getParser();
        }

    }
}
