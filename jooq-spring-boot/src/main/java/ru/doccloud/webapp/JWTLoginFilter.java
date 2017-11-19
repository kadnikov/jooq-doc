package ru.doccloud.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTLoginFilter.class);

    JWTLoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException, IOException, ServletException {
        traceHttpServletRequest("attemptAuthentication()", req);

        AccountCredentials creds = new ObjectMapper()
                .readValue(req.getInputStream(), AccountCredentials.class);
        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        creds.getUsername(),
                        creds.getPassword(),
                        Collections.emptyList()
                )
        );
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest req,
            HttpServletResponse res, FilterChain chain,
            Authentication auth) throws IOException, ServletException {
        TokenAuthenticationService
                .addAuthentication(res, auth.getName());
    }

    private void traceHttpServletRequest(String methodName, HttpServletRequest httpRequest){
        LOGGER.trace("{} traceHttpServletRequest(): Header: {} : {}", methodName, httpRequest.getHeader("authorization"));
//        if(LOGGER.isTraceEnabled()){
//            Enumeration<String> headerNames = httpRequest.getHeaderNames();
//
//            if (headerNames != null) {
//                while (headerNames.hasMoreElements()) {
//                    LOGGER.trace("{} traceHttpServletRequest(): Header: {}", methodName, httpRequest.getHeader(headerNames.nextElement()));
//                }
//            }
//        }
    }
}