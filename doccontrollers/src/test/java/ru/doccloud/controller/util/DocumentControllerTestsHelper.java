package ru.dodcloud.document.controller.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * Created by Illia_Ushakov on 10/21/2017.
 */
public class DocumentControllerTestsHelper {

    private final static String SERVER_URL = "http://localhost:8888/jooq/api/docs";
    private static final String DEFAULT_USER = "test";
    private static final String DEFAULT_PASS = "123456";

     public static HttpClientContext getHttpClientContext(){
        HttpHost targetHost = new HttpHost(SERVER_URL);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(DEFAULT_USER, DEFAULT_PASS));

        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

// Add AuthCache to the execution context
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        return context;
    }
}
