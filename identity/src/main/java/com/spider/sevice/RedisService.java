/*
 * Copyright (c) 2020 RECOBO
 */

package com.spider.sevice;

import com.spider.common.constant.CacheMap;
import com.spider.common.dto.UserSessionDTO;
import com.spider.repository.tech.UserRoleLinkRepository;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;

@Service
public class RedisService {
    private final RMap<String, UserSessionDTO> userSessionMap;
    private final RMap<Long, Set<String>> userSessionIdMap;
    private final RMap<Long, Collection<Long>> rolePrivilegeMap;
    private final UserRoleLinkRepository userRoleLinkRepository;


    public RedisService(RedissonClient redissonClient,
                        UserRoleLinkRepository userRoleLinkRepository) {
        // V2 isolates sessions written with the old Kryo codec. Typed JSON avoids class-name
        // metadata in Redis, so application/classloader changes cannot corrupt Date fields.
        userSessionMap = redissonClient.getMapCache(
                CacheMap.USER_SESSION_MAP_V2.toString(),
                new TypedJsonJacksonCodec(String.class, UserSessionDTO.class));
        userSessionMap.expire(Instant.now().plusSeconds(24 * 60 * 60));
        userSessionIdMap = redissonClient.getMapCache(CacheMap.USER_SESSION_ID_MAP_V2.toString());
        userSessionIdMap.expire(Instant.now().plusSeconds(24 * 60 * 60));
        rolePrivilegeMap = redissonClient.getMapCache(CacheMap.PRIVILEGE.toString());
        rolePrivilegeMap.expire(Instant.now().plusSeconds(24 * 60 * 60));
        this.userRoleLinkRepository = userRoleLinkRepository;
    }

    public UserSessionDTO getUserSessionDTO(String sessionKey) {
        return userSessionMap.get(sessionKey);
    }

    public Collection<UserSessionDTO> getUserSessionAllData() {
        return userSessionMap.values();
    }

    public Collection<UserSessionDTO> getSessionsOfUser(String sessionKey) {
        UserSessionDTO currentSession = getUserSessionDTO(sessionKey);
        if (currentSession == null) {
            return Collections.emptyList();
        }
        return userSessionMap.values().stream()
                .filter(session -> Objects.equals(session.getUserId(), currentSession.getUserId()))
                .toList();
    }

    public void removeAllSessionsOfUser(String sessionKey) {
        UserSessionDTO currentSession = getUserSessionDTO(sessionKey);
        if (currentSession == null) {
            return;
        }
        Set<String> sessionKeys = userSessionIdMap.remove(currentSession.getUserId());
        if (!CollectionUtils.isEmpty(sessionKeys)) {
            sessionKeys.forEach(userSessionMap::remove);
        }
    }

    public void putUserSessionDTO(UserSessionDTO userSessionDTO, boolean isNew) {
        userSessionDTO.setLastAccessDate(new Date());
        // set entry with 24 hours TTL so sessions expire automatically in Redis
        userSessionMap.fastPut(userSessionDTO.getUuid(), userSessionDTO);
        if (isNew) {
            addUserSession(userSessionDTO.getUserId(), userSessionDTO.getUuid());
        }
    }

    /**
     * Remove user session dto.
     *
     * @param sessionKey the session key
     */
    public void removeUserSessionDTO(String sessionKey) {
        UserSessionDTO userSessionDTO = getUserSessionDTO(sessionKey);
        if (userSessionDTO == null) {
            return;
        }
        userSessionMap.remove(userSessionDTO.getUuid());
        removeUserSession(userSessionDTO.getUserId(), userSessionDTO.getUuid());
    }

    /**
     * Clear all session.
     */
    public void clearAllSession() {
        userSessionMap.clear();
        userSessionIdMap.clear();
    }

    /**
     * Gets privilege.
     *
     * @param roleId the role id
     * @return the privilege
     */
    public Collection<Long> getPrivilege(Long roleId) {
        return rolePrivilegeMap.get(roleId);
    }

    /**
     * Put privilege.
     *
     * @param roleId       the role id
     * @param privilegeIds the privilege ids
     */
    public void putPrivilege(Long roleId, Collection<Long> privilegeIds) {
        rolePrivilegeMap.put(roleId, privilegeIds);
    }

    /**
     * Clear privilege.
     */
    public void clearPrivilege() {
        rolePrivilegeMap.clear();
    }

    private void removeUserSession(Long userId, String sessionKey) {
        Set<String> sessions = userSessionIdMap.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            sessions.remove(sessionKey);
            userSessionIdMap.put(userId, sessions);
        }
    }

    private void addUserSession(Long userId, String email) {
        Set<String> sessions = userSessionIdMap.get(userId);
        if (sessions == null) {
            sessions = new HashSet<>();
        }
        sessions.add(email);
        userSessionIdMap.put(userId, sessions);
    }

    /**
     * Gets session id map by role id.
     *
     * @param roleIds the role ids
     * @return the session id map by role id
     */
    public Map<Long, Set<String>> getSessionIdMapByRoleId(Set<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new HashMap<>();
        }
        Set<Long> userIds = userRoleLinkRepository.getUserIdsByRoleId(roleIds);
        Map<Long, Set<String>> map = new HashMap<>();
        if (!CollectionUtils.isEmpty(userIds)) {
            userIds.forEach(id -> {
                Set<String> sessionIds = userSessionIdMap.get(id);
                if (!CollectionUtils.isEmpty(sessionIds)) {
                    map.put(id, sessionIds);
                }
            });
        }
        return map;
    }

    /**
     * Clear sessions by user ids.
     *
     * @param userIds the user ids
     */
    public void clearSessionsByUserIds(Set<Long> userIds) {
        userIds.forEach(id -> {
            Set<String> sessionIds = userSessionIdMap.get(id);
            if (!CollectionUtils.isEmpty(sessionIds)) {
                sessionIds.forEach(userSessionMap::remove);
                userSessionIdMap.remove(id);
            }
        });
    }
}
