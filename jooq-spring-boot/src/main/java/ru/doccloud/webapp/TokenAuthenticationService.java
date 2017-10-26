package ru.doccloud.webapp;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security
        .authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Enumeration;

import static java.util.Collections.emptyList;

class TokenAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationService.class);

    static void addAuthentication(HttpServletResponse res, String username) {
        String JWT = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + JWTTokenGenerator.INSTANCE.getExpirationtime()))
                .signWith(SignatureAlgorithm.HS512, JWTTokenGenerator.INSTANCE.getSecretKey())
                .compact();
        res.addHeader(JWTTokenGenerator.INSTANCE.getHeaderString(), JWTTokenGenerator.INSTANCE.getTokenPrefix()
                + " " + JWT);
    }

    static Authentication getAuthentication(HttpServletRequest request) {
        traceHttpServletRequest("getAuthentication", request);
        String token = request.getHeader(JWTTokenGenerator.INSTANCE.getHeaderString());
        LOGGER.trace("getAuthentication(): jwtToken {}", token);
        if (token != null) {
            // parse the token.
            String user = Jwts.parser()
                    .setSigningKey(JWTTokenGenerator.INSTANCE.getSecretKey())
                    .parseClaimsJws(token.replace(JWTTokenGenerator.INSTANCE.getTokenPrefix(), ""))
                    .getBody()
                    .getSubject();

            return user != null ?
                    new UsernamePasswordAuthenticationToken(user, null, emptyList()) :
                    null;
        }
        return null;
    }

//    todo move this method from here and from JWTLoginFilter to common
    private static void traceHttpServletRequest(String methodName, HttpServletRequest httpRequest){
        if(LOGGER.isTraceEnabled()){
            Enumeration<String> headerNames = httpRequest.getHeaderNames();

            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    LOGGER.trace("{} traceHttpServletRequest(): Header: {} : {}", methodName, headerNames.nextElement()!= null ? headerNames.nextElement() : "", httpRequest.getHeader(headerNames.nextElement()));
                }
            }
        }
    }
}
