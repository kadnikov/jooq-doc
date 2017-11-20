package ru.doccloud.webapp;

import java.util.*;

/**
 * Created by ilya on 4/25/17.
 */
public enum JWTTokenGenerator {

    INSTANCE;

    private String secretKey;

    private final long expirationtime = 864_000_000; // 10 days
    private final String tokenPrefix = "Bearer";
    private final String jwtHeaderAuth = "cmisJwtAuthorization";
    private final String standardHeaderAuth = "Authorization";

    void generateRandomSecretKey(){
        secretKey = UUID.randomUUID().toString();
    }

    public synchronized String getSecretKey() {
        return secretKey;
    }

    public long getExpirationtime() {
        return expirationtime;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public String getJwtHeaderAuth() {
        return jwtHeaderAuth;
    }

    public String getStandardHeaderAuth() {
        return standardHeaderAuth;
    }
}