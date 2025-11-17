package com.spider.common.filter;

import com.spider.common.AppConstants;
import com.spider.common.utils.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Order(1) // Lower value means higher priority
@WebFilter("/*")
@Component
public class AuthenticationFilter extends BaseFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse,
                                    FilterChain chain) throws ServletException, IOException {
        // Logic to check each request
        System.out.println("Request intercepted by AuthenticationFilter");

        String uri = httpRequest.getRequestURI();

        // Allow requests starting with "public/*"
        if (uri.startsWith("/public/")) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }
        String authToken = httpRequest.getHeader(AppConstants.AUTHORIZATION);
        if (authToken != null && authToken.startsWith(jwtTokenUtil.getJwtConfiguration().getPrefix()+" ")) {
            String token = authToken.substring(7);
            boolean isValid = jwtTokenUtil.validateToken(token);
            if (isValid) {
                chain.doFilter(httpRequest, httpResponse);
            } else {
                unAuthorizedAccess(httpResponse,"Invalid authentication token", HttpStatus.UNAUTHORIZED.value());
            }
        }  else {
            unAuthorizedAccess(httpResponse,"Authentication token is missing or invalid", HttpStatus.UNAUTHORIZED.value());
        }
    }
}