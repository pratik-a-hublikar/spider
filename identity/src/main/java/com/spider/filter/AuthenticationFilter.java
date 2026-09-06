package com.spider.filter;

import com.spider.common.dto.UserSessionDTO;
import com.spider.sevice.RedisService;
import com.spider.sevice.tech.UserSessionService;
import com.spider.enity.core.User;
import com.spider.repository.core.UserRepository;
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
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RedisService redisService;

    private final UserSessionService userSessionService;
    private final MessageSource messageSource;
    private final UserRepository userRepository;

    public AuthenticationFilter(RedisService redisService, UserSessionService userSessionService,
                                MessageSource messageSource,
                                UserRepository userRepository) {
        this.redisService = redisService;
        this.userSessionService = userSessionService;
        this.messageSource = messageSource;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // Skip login and public endpoints
        return path.startsWith("/public")
                || path.startsWith("/auth/login")
                || path.startsWith("/auth/logins")
                || path.startsWith("/auth/identity/session")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, resolve("auth.missing.header"));
            return;
        }
        String token = authorization.startsWith(BEARER_PREFIX) ? authorization.substring(BEARER_PREFIX.length()).trim() : authorization.trim();
        UserSessionDTO session = redisService.getUserSessionDTO(token);
        if(session == null) {
            session = userSessionService.findOneByTokenAndActive(token);
        }
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, resolve("auth.invalid.token"));
            return;
        }
        User authenticatedUser = userRepository.findOneActiveById(session.getUserId());
        if (authenticatedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, resolve("auth.invalid.session"));
            return;
        }
        // The user record is authoritative. This also repairs DB-restored or stale cached
        // sessions that did not contain the Super Admin flag.
        session.setSuperAdmin(authenticatedUser.isSuperAdmin());
        request.setAttribute("userSession", session);
        request.setAttribute("userId", session.getUserId());

        // extra safety: ensure not older than 24 hours from loginDate
        Date login = session.getLoginDate();
        if (login != null && (System.currentTimeMillis() - login.getTime() > TimeUnit.HOURS.toMillis(24))) {
            redisService.removeUserSessionDTO(token);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, resolve("auth.token.expired"));
            return;
        }

        // refresh last access/ttl
        redisService.putUserSessionDTO(session, false);

        // expose session to downstream handlers
        request.setAttribute("userSession", session);
        filterChain.doFilter(request, response);
    }

    private String resolve(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }
}
