package com.marian.owncloudbackend.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marian.owncloudbackend.exceptions.LoginErrorException;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomAuthorisationFilter extends OncePerRequestFilter {

    @Value("${application.secret:secret}")
    private String secret ;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals("/login") ||
                request.getServletPath().equals("/user/register")) {
            filterChain.doFilter(request, response);
        } else {
//            String authorisationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            Cookie[] cookies = request.getCookies();
            Cookie jwtCookie = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("app-jwt")){
                    jwtCookie = cookie;
                    break;
                }
            }

            if (jwtCookie != null && !StringUtils.isEmpty(jwtCookie.getValue())) {
                try {
                    String token = jwtCookie.getValue();
                    Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());

                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String username = decodedJWT.getSubject();
                    String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    Arrays.stream(roles)
                            .forEach(value -> {
                                authorities.add(new SimpleGrantedAuthority(value));
                            });

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,null,authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request,response);
                } catch (Exception exception) {
                    log.error("Error logging in: {}",exception.getMessage());
                    throw new LoginErrorException("Login has failed");
                }

            }else {
                filterChain.doFilter(request,response);
            }
        }
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
