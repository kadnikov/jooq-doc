package ru.doccloud.controller;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import ru.doccloud.controller.util.DocumentControllerTestsHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DocumentControllerPostTest {
    private final static String HTTP_SCHEMA_URL = "http";

    private final static String SERVER_URL = "http://localhost:8888/jooq/api/docs";
    private final static String URL = "localhost";
    private final static int PORT = 8888;
    private final static String UPDATE_PATH= "jooq/api/docs/updatecontent/";
    private final static String ADD_CONTENT_PATH = "jooq/api/docs/addcontent/";
    private final static String BASIC_PATH = "jooq/api/docs/";

    private final static String FILE_PATH = "C:\\trworkspace\\intended_new.txt";
    private final static String UPDATE_FILE_PATH = "C:\\trworkspace\\intended1.txt";

    private HttpClient httpClient;

    @Before
    public void init(){
        httpClient = HttpClientBuilder.create().build();
        login();
    }

//    @Test
    public void loginTest(){
        login();
    }

    @Test
    public void addDocTest(){
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(ADD_CONTENT_PATH);
            URI uri = uriBuilder.build();
            HttpPost httpPost = new HttpPost(uri );
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

// This attaches the file to the POST:
            File f = new File(FILE_PATH);
            builder.addBinaryBody(
                    "file",
                    new FileInputStream(f),
                    ContentType.APPLICATION_OCTET_STREAM,
                    f.getName()
            );

            HttpEntity multipart = builder.build();

            httpPost.setEntity(multipart);
                HttpResponse response = null;

            response = httpClient.execute(httpPost, DocumentControllerTestsHelper.getHttpClientContext());
            int respStatus = response.getStatusLine().getStatusCode();
            System.out.println("response status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void addDocWithoutFileTest(){
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(BASIC_PATH);
            URI uri = uriBuilder.build();
            HttpPost httpPost = new HttpPost(uri );
            httpPost.setEntity(new StringEntity(
                    prepareDtoAsJSON(),
                    ContentType.create("application/json")));
            HttpResponse response = httpClient.execute(httpPost, DocumentControllerTestsHelper.getHttpClientContext());
            int respStatus = response.getStatusLine().getStatusCode();
            System.out.println("response status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void updateContentTest(){
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(UPDATE_PATH+27);
            URI uri = uriBuilder.build();

            HttpPost httpPost = new HttpPost(uri);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("part1", "yes ", ContentType.TEXT_PLAIN);
            File f = new File(UPDATE_FILE_PATH);
            builder.addBinaryBody(
                    "file",
                    new FileInputStream(f),
                    ContentType.APPLICATION_OCTET_STREAM,
                    f.getName()
            );

            HttpEntity multipart = builder.build();

            httpPost.setEntity(multipart);
            HttpResponse response = null;
            response = httpClient.execute(httpPost, DocumentControllerTestsHelper.getHttpClientContext());
            int respStatus = response.getStatusLine().getStatusCode();
            System.out.println("response login status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


//    @Test
    public void updateDocTest(){
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(BASIC_PATH + 27);
            URI uri = uriBuilder.build();
            HttpPut httpPut = new HttpPut(uri);

            httpPut.setEntity(new StringEntity(
                    prepareDtoAsJSON(),
                    ContentType.create("application/json")));
            HttpResponse response = httpClient.execute(httpPut, DocumentControllerTestsHelper.getHttpClientContext());
            int respStatus = response.getStatusLine().getStatusCode();
            System.out.println("response status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


//    @Test
    public void deleteDocTest() {
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(BASIC_PATH + 27);
            URI uri = uriBuilder.build();

            HttpDelete httpDelete = new HttpDelete(uri);
            HttpResponse response = httpClient.execute(httpDelete, DocumentControllerTestsHelper.getHttpClientContext());
            int respStatus = response.getStatusLine().getStatusCode();
            System.out.println("response login status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void login(){
        HttpResponse response = null;
        try {
            response = httpClient.execute(new HttpGet(SERVER_URL), DocumentControllerTestsHelper.getHttpClientContext());
            int statusLoginCode = response.getStatusLine().getStatusCode();
            System.out.println("response login status: " + statusLoginCode + "os ok ? " + (statusLoginCode == HttpStatus.SC_OK));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String prepareDtoAsJSON(){
        return "{\"title\":\"xyz1\",\"type\":\"document\"}";
    }
}
