package com.spider.filter;

import com.spider.common.dto.UserSessionDTO;
import com.spider.dto.PrivilegeDTO;
import com.spider.sevice.RedisService;
import com.spider.sevice.tech.PrivilegeMasterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AuthorizationFilter extends OncePerRequestFilter {

    private final PrivilegeMasterService privilegeMasterService;
    private final RedisService redisService;
    private final MessageSource messageSource;

    public AuthorizationFilter(PrivilegeMasterService privilegeMasterService, RedisService redisService,
                               MessageSource messageSource) {
        this.privilegeMasterService = privilegeMasterService;
        this.redisService = redisService;
        this.messageSource = messageSource;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        String method = request.getMethod();

        // lookup privilege by uri+method
        var privilege = privilegeMasterService.get(path, method);
        if (privilege == null) {
            // no privilege configured for this endpoint -> allow
            filterChain.doFilter(request, response);
            return;
        }

        UserSessionDTO session = (UserSessionDTO) request.getAttribute("userSession");
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, resolve("auth.invalid.session"));
            return;
        }

        Collection<Long> userPrivileges = privilegeMasterService.getPrivilegesByUserId(session.getUserId());
        if (userPrivileges == null || !userPrivileges.contains(privilege.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, resolve("auth.forbidden"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolve(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
