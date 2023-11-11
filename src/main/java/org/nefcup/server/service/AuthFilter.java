/*
    Copyright 2023 Dmitrij Kulabuhov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
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
