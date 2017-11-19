package ru.doccloud.controller;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import ru.doccloud.controller.util.DocumentControllerTestsHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DocumentControllerGetTest {

    private final static String LOGIN_PATH = "jooq/login";
    private final static String HTTP_SCHEMA_URL = "http";

    private final static String URL = "localhost";
    private final static int PORT = 8888;
    private final static String BASIC_PATH = "jooq/api/docs/";

    private final static String GET_CONTENT_PATH = BASIC_PATH + "getcontent/";

    private final static String GET_UUID_PATH = BASIC_PATH + "uuid/";

    private static final String PATH_TO_FILE = "C:\\trworkspace\\test_read_file\\read.txt";




    private HttpClient httpClient;

    @Before
    public void init(){
        httpClient = HttpClientBuilder.create().build();
    }

    //    @Test
    public void getDocByIdTest(){
        try {
            final String token = getJwtToken();
            if(!StringUtils.isBlank(token)) {
                URIBuilder uriBuilder = new URIBuilder();
                uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(BASIC_PATH + 23);
                URI uri = uriBuilder.build();
                HttpGet httpGet = new HttpGet(uri);
                httpGet.addHeader("Authorization", token);
                HttpResponse response = httpClient.execute(httpGet, DocumentControllerTestsHelper.getHttpClientContext());
                int respStatus = response.getStatusLine().getStatusCode();
                System.out.println("response  status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));

                if (respStatus == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    try {
                        entity.writeTo(os);
                    } catch (IOException e1) {
                    }
                    String contentString = new String(os.toByteArray());

                    System.out.println("contentString  \n " + contentString);
                }
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    public void getDocByUUIDTest(){
        try {
            final String token = getJwtToken();
            if(!StringUtils.isBlank(token)) {
                URIBuilder uriBuilder = new URIBuilder();
                uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(GET_UUID_PATH + "857353a7-2f0d-4c25-9583-ffbb36e47d75");
                URI uri = uriBuilder.build();
                HttpGet httpGet = new HttpGet(uri);
                httpGet.addHeader("Authorization", token);
                HttpResponse response = httpClient.execute(httpGet, DocumentControllerTestsHelper.getHttpClientContext());
                int respStatus = response.getStatusLine().getStatusCode();
                System.out.println("response  status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));

                if (respStatus == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();

                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    try {
                        entity.writeTo(os);
                    } catch (IOException e1) {
                    }
                    String contentString = new String(os.toByteArray());

                    System.out.println("contentString  \n " + contentString);
                }
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getContentTest(){
        try {
            final String token = getJwtToken();
            if(!StringUtils.isBlank(token)) {
                URIBuilder uriBuilder = new URIBuilder();
                uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(GET_CONTENT_PATH + 23);
                URI uri = uriBuilder.build();
                HttpGet httpGet = new HttpGet(uri);
                httpGet.addHeader("Authorization", token);
                HttpResponse response = httpClient.execute(httpGet, DocumentControllerTestsHelper.getHttpClientContext());
                int respStatus = response.getStatusLine().getStatusCode();
                System.out.println("response  status: " + respStatus + "os ok ? " + (respStatus == HttpStatus.SC_OK));

                if (respStatus == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();

                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    try {
                        entity.writeTo(os);
                    } catch (IOException e1) {
                    }
                    readByteArrToFile(os.toByteArray());
                }
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private void readByteArrToFile(byte[] bytes){
        try {
            FileUtils.writeByteArrayToFile(new File(PATH_TO_FILE), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private String getJwtToken(){
        HttpResponse response = null;
        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme(HTTP_SCHEMA_URL).setHost(URL).setPort(PORT).setPath(LOGIN_PATH);
            URI uri = uriBuilder.build();
            HttpPost httpPost = new HttpPost(uri );
            httpPost.setEntity(new StringEntity(
                    prepareLoginInfoAsJSON(),
                    ContentType.create("application/json")));
            response = httpClient.execute(httpPost, DocumentControllerTestsHelper.getHttpClientContext());
            int statusLoginCode = response.getStatusLine().getStatusCode();

            if(statusLoginCode == HttpStatus.SC_OK) {
                final String token = response.getFirstHeader("authorization").getValue();

                System.out.println("JWT token: " + token);
                return token;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String prepareLoginInfoAsJSON(){
        return "{\"username\":\"test\",\"password\":\"123456\"}";
    }

}