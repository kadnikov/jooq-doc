package ru.doccloud.webapp;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class TokenAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationService.class);

    static void addAuthentication(HttpServletResponse res, String username) {
        final String jwt = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + JWTTokenGenerator.INSTANCE.getExpirationtime()))
                .signWith(SignatureAlgorithm.HS512, JWTTokenGenerator.INSTANCE.getSecretKey())
                .compact();
        String loginResponse;
		try {
			loginResponse = new ObjectMapper().writeValueAsString(new LoginResponse(jwt));
		

	        LOGGER.trace("addAuthentication(): login response {}", loginResponse);
	
	        res.setHeader("Accept", "application/json");
	        res.setHeader("Content-type", "application/json");
	
	        res.getWriter().write(loginResponse);
	        res.getWriter().flush();
	        res.getWriter().close();
         
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        res.addHeader(JWTTokenGenerator.INSTANCE.getStandardHeaderAuth(), JWTTokenGenerator.INSTANCE.getTokenPrefix()
                + " " + jwt);
    }

    static Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(JWTTokenGenerator.INSTANCE.getJwtHeaderAuth());
        if(StringUtils.isBlank(token)) {
            token = request.getHeader(JWTTokenGenerator.INSTANCE.getStandardHeaderAuth());
        }
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
    
    @SuppressWarnings("unused")
    private static class LoginResponse {
        public String access_token;
        public String refresh_token;
        public String token_type;
        public Integer expires_in;
		public String scope;

        public LoginResponse(final String token) {
            System.out.println(String.format("LoginResponse token: %s", token));
            this.access_token = token;
            this.refresh_token= token;
            this.token_type="Bearer";
            this.expires_in=2000;
            this.scope="administration compliance search";
        }

        @Override
        public String toString() {
            return "LoginResponse{" +
                    "token='" + access_token + '\'' +
                    '}';
        }
    }
}