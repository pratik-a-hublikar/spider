package com.spider.sevice.tech.impl;


import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.response.identity.UserDTO;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.core.User;
import com.spider.enity.tech.RoleMaster;
import com.spider.enity.tech.UserRoleLink;
import com.spider.repository.core.UserLocationLinkRepository;
import com.spider.repository.core.UserOrganizationLinkRepository;
import com.spider.repository.core.UserRepository;
import com.spider.repository.tech.UserRoleLinkRepository;
import com.spider.sevice.tech.RoleMasterService;
import com.spider.sevice.tech.UserRoleLinkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Log4j2
@Service
public class UserRoleLinkServiceImpl extends CommonServiceImpl<UserRoleLink,Long> implements UserRoleLinkService {

    private final UserRoleLinkRepository repository;
    private final CriteriaUtil<UserRoleLink> criteriaUtil;
    private final RoleMasterService roleMasterService;
    private final UserRepository userRepository;
    private final UserOrganizationLinkRepository userOrganizationLinkRepository;
    private final UserLocationLinkRepository userLocationLinkRepository;
    @Autowired
    public UserRoleLinkServiceImpl(UserRoleLinkRepository repository,
                                   CriteriaUtil<UserRoleLink> criteriaUtil,
                                   RoleMasterService roleMasterService,
                                   UserRepository userRepository,
                                   UserOrganizationLinkRepository userOrganizationLinkRepository,
                                   UserLocationLinkRepository userLocationLinkRepository) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.roleMasterService = roleMasterService;
        this.userRepository = userRepository;
        this.userOrganizationLinkRepository = userOrganizationLinkRepository;
        this.userLocationLinkRepository = userLocationLinkRepository;
    }


    @Override
    protected ParentRepository<UserRoleLink, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<UserRoleLink> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    @Transactional
    public void replaceLinks(Long userId, List<Long> roleIds) {
        List<Long> requestedIds = roleIds == null ? List.of() : roleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UserRoleLink> existingLinks = repository.findByUserId(userId);

        existingLinks.forEach(link -> {
            if (!requestedIds.contains(link.getRoleId())) {
                link.setIsActive(false);
                link.setIsDeleted(true);
                repository.save(link);
            }
        });

        requestedIds.forEach(roleId -> {
            RoleMaster role = roleMasterService.get(roleId);
            if (role == null) {
                throw new ValidationException("role.not.found");
            }
            UserRoleLink link = existingLinks.stream()
                    .filter(existing -> roleId.equals(existing.getRoleId()))
                    .findFirst()
                    .orElseGet(UserRoleLink::new);
            link.setUserId(userId);
            link.setRoleId(roleId);
            link.setOrgId(role.getOrgId());
            link.setIsActive(true);
            link.setIsDeleted(false);
            repository.save(link);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getActiveRoleIds(Long userId) {
        return repository.findByUserId(userId).stream()
                .filter(link -> Boolean.TRUE.equals(link.getIsActive())
                        && !Boolean.TRUE.equals(link.getIsDeleted()))
                .map(UserRoleLink::getRoleId)
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
    public UserDTO assignUserOrgRole(Long userId, Long roleId, Long orgId) {
        RoleMaster role = roleMasterService.get(roleId);
        if (!role.getOrgId().equals(orgId)) {
            throw new ValidationException("role.not.found");
        }
        List<UserRoleLink> userRoles = getRoleLinkOfUser(userId, orgId);
        List<Long> existRoles = userRoles.stream().map(UserRoleLink::getRoleId).toList();
        if (existRoles.contains(roleId)) {
            return toUserDto(userId);
        }
        for (UserRoleLink userRole : userRoles) {
            userRole.setIsActive(false);
            userRole.setUpdatedAt(new Date());
        }
        UserRoleLink newRole = UserRoleLink.builder().roleId(roleId).userId(userId).orgId(orgId).build();
        userRoles.add(newRole);
        repository.saveAll(userRoles);
//        userSessionService.flushUserSessions(userId);
        repository.flush();
        return toUserDto(userId);
    }

    @Override
    @Transactional
    public UserDTO revokeUserOrgRole(Long userId, Long orgId) {
        List<UserRoleLink> userRoles = getRoleLinkOfUser(userId, orgId);
        userRoles.forEach(userRole -> {
            userRole.setIsActive(false);
            userRole.setIsDeleted(true);
        });
        repository.saveAll(userRoles);
        repository.flush();
        return toUserDto(userId);
    }

    public List<UserRoleLink> getRoleLinkOfUser(Long userId, Long orgId) {
        return repository.findByUserIdAndActive(userId, true, orgId);
    }

    private UserDTO toUserDto(Long userId) {
        User user = userRepository.findOneActiveById(userId);
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .email(user.getEmail())
                .fname(user.getFname())
                .lname(user.getLname())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .statusId(user.getStatusId())
                .superAdmin(user.isSuperAdmin())
                .orgIdList(userOrganizationLinkRepository.findActiveOrganizationIdsByUserId(userId).stream().toList())
                .locationIdList(userLocationLinkRepository.findActiveLocationIdsByUserId(userId).stream().toList())
                .roleIdList(repository.findActiveRoleIdsByUserId(userId).stream().toList())
                .build();
    }
}
