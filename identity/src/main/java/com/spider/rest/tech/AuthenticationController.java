package com.spider.rest.tech;

import com.spider.common.dto.UserSessionDTO;
import com.spider.common.request.identity.LoginRequest;
import com.spider.common.response.CommonResponse;
import com.spider.common.rest.BaseResource;
import com.spider.dto.LoginDTO;
import com.spider.enity.tech.UserSession;
import com.spider.enity.core.User;
import com.spider.repository.core.UserRepository;
import com.spider.sevice.RedisService;
import com.spider.sevice.tech.UserSessionService;
import com.spider.sevice.core.UserService;
import com.spider.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RequestMapping("/auth")
@RestController
@Tag(name = "Authentication")
public class AuthenticationController extends BaseResource {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final UserSessionService userSessionService;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private static final String BEARER_PREFIX = "Bearer ";

    public AuthenticationController(UserRepository userRepository,
                                    BCryptPasswordEncoder passwordEncoder,
                                    RedisService redisService,
                                    UserSessionService userSessionService,
                                    JwtUtils jwtUtils,
                                    UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
        this.userSessionService = userSessionService;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Authenticate a user", description = "Returns an access token and user identity details.")
    public ResponseEntity<CommonResponse<LoginDTO>> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(CommonResponse.of(null, resolve("login.failed")));
        }

        String token = jwtUtils.generateToken(user);

        // build session DTO
        UserSessionDTO sessionDTO = new UserSessionDTO();
        sessionDTO.setUuid(token);
        sessionDTO.setUserId(user.getId());
        sessionDTO.setEmail(user.getEmail());
        sessionDTO.setLoginDate(new Date());
        sessionDTO.setTimeout(24 * 60); // minutes
        sessionDTO.setStayLoggedIn(request.isStayLoggedIn());
        sessionDTO.setSuperAdmin(user.isSuperAdmin());

        // persist to DB
        UserSession dbSession = persistToDB(user, sessionDTO, token);
        userSessionService.create(dbSession);

        // put in redis (maps token -> session via uuid search logic in RedisService)
        // store keyed by email as existing redisService expects
        // lastAccessDate and TTL handled by RedisService
        redisService.putUserSessionDTO(sessionDTO, true);

        LoginDTO dto = new LoginDTO();
        dto.setFirstName(user.getFname());
        dto.setLastName(user.getLname());
        dto.setEmail(user.getEmail());
        dto.setAccessToken(token);
        dto.setUser(userService.getById(user.getId()));

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .body(CommonResponse.of(dto, resolve("login.success")));
    }

    private static @NonNull UserSession persistToDB(User user, UserSessionDTO sessionDTO, String token) {
        UserSession dbSession = new UserSession();
        dbSession.setUserId(user.getId());
        dbSession.setEmail(user.getEmail());
        dbSession.setLoginDate(sessionDTO.getLoginDate());
        dbSession.setTimeout(sessionDTO.getTimeout());
        dbSession.setStayLoggedIn(sessionDTO.isStayLoggedIn());
        dbSession.setIpAddress(null);
        dbSession.setUserAgent(null);
        dbSession.setCreatedBy(user.getId());
        dbSession.setCreatedAt(new Date());
        dbSession.setToken(token);
        return dbSession;
    }
}
