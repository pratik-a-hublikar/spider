package com.spider.common.filter;

import com.spider.auth.service.UserMasterService;
import com.spider.common.AppConstants;
import com.spider.common.utils.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Order(2) // Lower value means higher priority
@WebFilter("/*")
@Component
@Log4j2
public class AuthorizationFilter  extends BaseFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMasterService userMasterService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse,
                                    FilterChain chain) throws ServletException, IOException {
       log.info("entered AuthorizationFilter");
       String uri = httpRequest.getRequestURI();
        if (uri.startsWith("/public/")) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        String authToken = httpRequest.getHeader(AppConstants.AUTHORIZATION);
        if (authToken != null && authToken.startsWith(jwtTokenUtil.getJwtConfiguration().getPrefix()+" ")) {
            String token = authToken.substring(7);
            String uuid = jwtTokenUtil.decodeUUID(token);
            if (StringUtils.hasLength(uuid)) {
                boolean hasAccess = userMasterService.hasAuthorization(uri,httpRequest.getMethod(),uuid);
                if(hasAccess){
                    httpRequest.setAttribute("userId",uuid);
                    chain.doFilter(httpRequest, httpResponse);
                }else{
                    unAuthorizedAccess(httpResponse,"User is not allowed to call the API", HttpStatus.UNAUTHORIZED.value());
                }
            } else {
                unAuthorizedAccess(httpResponse,"Token has been expired,please login again!", HttpStatus.UNAUTHORIZED.value());
            }
        }  else {
            unAuthorizedAccess(httpResponse,"Authentication token is missing or invalid", HttpStatus.UNAUTHORIZED.value());
        }
    }
}
