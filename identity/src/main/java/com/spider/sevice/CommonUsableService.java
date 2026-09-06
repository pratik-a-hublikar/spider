package com.spider.sevice;

import com.spider.repository.tech.UserRoleLinkRepository;
import com.spider.sevice.tech.ModuleAccessMasterService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@Log4j2
public class CommonUsableService {


    private final ModuleAccessMasterService moduleAccessService;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final RedisService redisService;
    @Autowired
    public CommonUsableService(ModuleAccessMasterService moduleAccessService,
                               RedisService redisService,
                               UserRoleLinkRepository userRoleLinkRepository) {
        this.moduleAccessService = moduleAccessService;
        this.redisService = redisService;
        this.userRoleLinkRepository = userRoleLinkRepository;
    }

    public void refreshACLByModuleAccess(Set<Long> moduleAccessIds) {
        try {
            Set<Long> userIds = getModuleAccessUsers(moduleAccessIds);
            redisService.clearSessionsByUserIds(userIds);
            redisService.clearPrivilege();
        } catch (Exception e) {
            log.error("Refresh ACL Error : ", e);
        }
    }

    private Set<Long> getModuleAccessUsers(Collection<Long> moduleAccessIds) {
        Set<Long> roleIds = moduleAccessService.getRoleOfModuleAccess(moduleAccessIds);
        Set<Long> userIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(moduleAccessIds)) {
            userIds = userRoleLinkRepository.getUserIdsByRoleId(roleIds);
        }
        return userIds;
    }

    public void refreshACL() {
        try {
            Set<Long> userIds = userRoleLinkRepository.getAllUserIds();
            redisService.clearSessionsByUserIds(userIds);
            redisService.clearPrivilege();
        } catch (Exception e) {
            log.error("Refresh ACL Error : ", e);
        }
    }
}
