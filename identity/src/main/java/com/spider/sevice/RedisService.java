/*
 * Copyright (c) 2020 RECOBO
 */

package com.spider.common.service.impl;

import com.spider.common.dto.UserSessionDTO;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class RedisService {
    private final RMap<String, UserSessionDTO> userSessionMap;
    private final RMap<Long, Set<String>> userSessionIdMap;
    private final RMap<Long, Collection<Long>> rolePrivilegeMap;
    private final UserRoleLinkRepository userRoleLinkRepository;

    /**
     * Instantiates a new Redis service.
     *
     * @param redissonClient         the redisson client
     * @param userRoleLinkRepository the user role link repository
     */
    public RedisService(RedissonClient redissonClient,
                        UserRoleLinkRepository userRoleLinkRepository) {
        userSessionMap = redissonClient.getMapCache(CacheMap.USER_SESSION_MAP.toString());
        userSessionIdMap = redissonClient.getMapCache(CacheMap.USER_SESSION_ID_MAP.toString());
        rolePrivilegeMap = redissonClient.getMapCache(CacheMap.PRIVILEGE.toString());
        this.userRoleLinkRepository = userRoleLinkRepository;
    }

    /**
     * Gets user session dto.
     *
     * @param sessionKey the session key
     * @return the user session dto
     */
    public UserSessionDTO getUserSessionDTO(String sessionKey) {
        return userSessionMap.get(sessionKey);
    }

    /**
     * Gets user session all data.
     *
     * @return the user session all data
     */
    public Collection<UserSessionDTO> getUserSessionAllData() {
        return userSessionMap.values();
    }

    /**
     * Put user session dto.
     *
     * @param userSessionDTO the user session dto
     * @param isNew          the is new
     */
    public void putUserSessionDTO(UserSessionDTO userSessionDTO, boolean isNew) {
        userSessionDTO.setLastAccessDate(new Date());
        userSessionMap.put(userSessionDTO.getEmail(), userSessionDTO);
        if (isNew) {
            addUserSession(userSessionDTO.getUserId(), userSessionDTO.getEmail());
        }
    }

    /**
     * Remove user session dto.
     *
     * @param sessionKey the session key
     */
    public void removeUserSessionDTO(String sessionKey) {
        UserSessionDTO userSessionDTO = userSessionMap.remove(sessionKey);
        if (userSessionDTO == null) {
            return;
        }
        removeUserSession(userSessionDTO.getUserId(), userSessionDTO.getEmail());
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
    public Map<Long, Set<String>> getSessionIdMapByRoleId(List<Long> roleIds) {
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
