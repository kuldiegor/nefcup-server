package org.nefcup.server.service;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class AuthFilter implements Filter {
    private static final String TOKEN_HEADER_NAME = "token";
    private final byte[] ourTokenBytes;

    public AuthFilter(@Value("${nefcup.token}") String token) {
        this.ourTokenBytes = token.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String token = request.getHeader(TOKEN_HEADER_NAME);
        if (token==null || token.isBlank()){
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }
        byte[] theirTokenBytes = token.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(ourTokenBytes,theirTokenBytes)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }
}
