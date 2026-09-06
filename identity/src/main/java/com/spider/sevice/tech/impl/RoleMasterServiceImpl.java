package com.spider.sevice.tech.impl;


import com.spider.common.dto.UserSessionDTO;
import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.response.identity.RoleDTO;
import com.spider.common.response.identity.RoleSummaryDTO;
import com.spider.common.response.identity.RoleModuleAccessDTO;
import com.spider.common.response.identity.UserDTO;
import com.spider.common.response.identity.UserSummaryDTO;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.core.User;
import com.spider.enity.tech.ModuleAccessMaster;
import com.spider.enity.tech.RoleMaster;
import com.spider.enity.tech.RoleModuleAccessLink;
import com.spider.enity.tech.RoleReportingRoleLink;
import com.spider.enity.tech.UserRoleLink;
import com.spider.repository.core.OrganizationRepository;
import com.spider.repository.core.UserOrganizationLinkRepository;
import com.spider.repository.core.UserRepository;
import com.spider.repository.tech.ModuleAccessMasterRepository;
import com.spider.repository.tech.RoleMasterRepository;
import com.spider.repository.tech.RoleModuleAccessLinkRepository;
import com.spider.repository.tech.RoleReportingRoleLinkRepository;
import com.spider.repository.tech.UserRoleLinkRepository;
import com.spider.sevice.CommonUsableService;
import com.spider.sevice.tech.RoleMasterService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class RoleMasterServiceImpl extends CommonServiceImpl<RoleMaster,Long> implements RoleMasterService {

    private final RoleMasterRepository repository;
    private final CriteriaUtil<RoleMaster> criteriaUtil;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final RoleReportingRoleLinkRepository roleReportingRoleLinkRepository;
    private final RoleModuleAccessLinkRepository roleModuleAccessLinkRepository;
    private final ModuleAccessMasterRepository moduleAccessMasterRepository;
    private final UserRepository userRepository;
    private final UserOrganizationLinkRepository userOrganizationLinkRepository;
    private final OrganizationRepository organizationRepository;
    private final CommonUsableService commonUsableService;
    @Autowired
    public RoleMasterServiceImpl(RoleMasterRepository repository,
                                 CriteriaUtil<RoleMaster> criteriaUtil,
                                 UserRoleLinkRepository userRoleLinkRepository,
                                 RoleReportingRoleLinkRepository roleReportingRoleLinkRepository,
                                 RoleModuleAccessLinkRepository roleModuleAccessLinkRepository,
                                 ModuleAccessMasterRepository moduleAccessMasterRepository,
                                 UserRepository userRepository,
                                 UserOrganizationLinkRepository userOrganizationLinkRepository,
                                 OrganizationRepository organizationRepository,
                                 CommonUsableService commonUsableService) {
        this.repository = repository;
        this.criteriaUtil = criteriaUtil;
        this.userRoleLinkRepository = userRoleLinkRepository;
        this.roleReportingRoleLinkRepository = roleReportingRoleLinkRepository;
        this.roleModuleAccessLinkRepository = roleModuleAccessLinkRepository;
        this.moduleAccessMasterRepository = moduleAccessMasterRepository;
        this.userRepository = userRepository;
        this.userOrganizationLinkRepository = userOrganizationLinkRepository;
        this.organizationRepository = organizationRepository;
        this.commonUsableService = commonUsableService;
    }


    @Override
    protected ParentRepository<RoleMaster, Long> getRepository() {
        return repository;
    }

    @Override
    protected CriteriaUtil<RoleMaster> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public List<RoleMaster> getByModuleAccess(Long id) {
        return enrich(repository.getByModuleAccessId(id));
    }

    @Override
    public List<RoleSummaryDTO> getByModuleAccessDtos(Long id, Long requesterId) {
        User requester = requesterId == null ? null : userRepository.findOneActiveById(requesterId);
        if (requester == null) throw new ValidationException("role.organization.access.denied");
        Set<Long> manageableRoleIds = getManageableRoleIds(requesterId);
        Set<Long> organizationIds = requester.isSuperAdmin()
                ? Collections.emptySet()
                : findAccessibleOrganizationIds(requesterId);
        return getByModuleAccess(id).stream()
                .filter(role -> requester.isSuperAdmin() || organizationIds.contains(role.getOrgId()))
                .map(role -> toRoleSummaryDto(role, manageableRoleIds.contains(role.getId()), countActiveRoleUsers(role.getId())))
                .toList();
    }

    @Override
    public RoleMaster get(Long id) {
        return enrich(repository.findOneActiveById(id));
    }

    @Override
    public Page<RoleMaster> filter(RecordFilter recordFilter) {
        Page<RoleMaster> roles = super.filter(recordFilter);
        enrich(roles.getContent());
        return roles;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleSummaryDTO> filterRoles(RecordFilter recordFilter, Long requesterId) {
        Page<RoleMaster> roles = super.filter(recordFilter);
        Set<Long> manageableRoleIds = getManageableRoleIds(requesterId);
        Map<Long, Long> userCounts = countActiveRoleUsersByRoleIds(
                roles.getContent().stream().map(RoleMaster::getId).toList());
        return roles.map(role -> toRoleSummaryDto(
                role,
                manageableRoleIds.contains(role.getId()),
                userCounts.getOrDefault(role.getId(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getByUuid(String roleUuid, Long requesterId) {
        RoleMaster role = requireRole(roleUuid);
        validateOrganizationAccess(role.getOrgId(), requesterId);
        RoleMaster enrichedRole = enrich(role);
        return toRoleDto(enrichedRole, getManageableRoleIds(requesterId).contains(role.getId()),
                countActiveRoleUsers(role.getId()));
    }

    @Override
    protected void addEntityDataSecurityCheck(RecordFilter recordFilter, UserSessionDTO userSession) {
        if (userSession == null || userSession.getUserId() == null
                || userRepository.findOneActiveById(userSession.getUserId()) == null) {
            appendAllowedOrganizationIds(recordFilter, Collections.emptySet());
            return;
        }

        Set<Long> accessibleOrganizationIds = new LinkedHashSet<>(
                userOrganizationLinkRepository.findActiveOrganizationIdsByUserId(userSession.getUserId()));
        Set<Long> organizationsToExpand = new LinkedHashSet<>(accessibleOrganizationIds);

        while (!organizationsToExpand.isEmpty()) {
            Set<Long> childOrganizationIds = new LinkedHashSet<>(
                    organizationRepository.findActiveChildOrganizationIds(organizationsToExpand));
            childOrganizationIds.removeAll(accessibleOrganizationIds);
            if (childOrganizationIds.isEmpty()) {
                break;
            }
            accessibleOrganizationIds.addAll(childOrganizationIds);
            organizationsToExpand = childOrganizationIds;
        }

        appendAllowedOrganizationIds(recordFilter, accessibleOrganizationIds);
    }

    private void appendAllowedOrganizationIds(RecordFilter recordFilter, Set<Long> organizationIds) {
        List<String> values = organizationIds.isEmpty()
                ? List.of("-1")
                : organizationIds.stream().sorted().map(String::valueOf).toList();
        recordFilter.appendCriteria("orgId", "in", values);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public void assignRole(long userId, Collection<Long> roleIds) {
        roleIds.forEach(r -> userRoleLinkRepository.save(UserRoleLink.builder().userId(userId).roleId(r).build()));
    }


    @Transactional
    @Override
    public void saveRole(RoleDTO roleDTO, Long requesterId) {
        validateMyAccessRequest(roleDTO, requesterId);
        validateOrganizationAccess(roleDTO.getOrgId(), requesterId);
        RoleMaster role = RoleMaster.builder().name(roleDTO.getName()).orgId(roleDTO.getOrgId()).build();
        role = repository.save(role);
        saveReportingRoles(role.getId(), roleDTO.getReportsToRoleIds());
        saveModuleAccesses(role, roleDTO.getModuleAccess(), requesterId);
        saveRoleUsers(role, roleDTO);
        commonUsableService.refreshACL();
    }

    @Override
    @Transactional
    public void updateRole(String roleUuid, RoleDTO roleDTO, Long requesterId) {
        RoleMaster roleMaster = requireRole(roleUuid);
        validateMyAccessRequest(roleDTO, requesterId);
        validateManageableRole(roleMaster.getId(), requesterId);
        validateOrganizationAccess(roleDTO.getOrgId(), requesterId);
        roleMaster.setName(roleDTO.getName());
        roleMaster.setOrgId(roleDTO.getOrgId());
        
        repository.save(roleMaster);
        saveReportingRoles(roleMaster.getId(), roleDTO.getReportsToRoleIds());
        saveModuleAccesses(roleMaster, roleDTO.getModuleAccess(), requesterId);
        saveRoleUsers(roleMaster, roleDTO);
        commonUsableService.refreshACL();
    }

    @Override
    public List<RoleSummaryDTO> getAllRoles(Long requesterId) {
        User requester = requesterId == null ? null : userRepository.findOneActiveById(requesterId);
        if (requester == null) throw new ValidationException("role.organization.access.denied");
        Set<Long> manageableRoleIds = getManageableRoleIds(requesterId);
        Set<Long> organizationIds = requester.isSuperAdmin()
                ? Collections.emptySet()
                : findAccessibleOrganizationIds(requesterId);
        List<RoleMaster> roles = repository.findAll().stream()
                .filter(role -> Boolean.TRUE.equals(role.getIsActive()) && !Boolean.TRUE.equals(role.getIsDeleted()))
                .filter(role -> requester.isSuperAdmin() || organizationIds.contains(role.getOrgId()))
                .toList();
        Map<Long, Long> userCounts = countActiveRoleUsersByRoleIds(roles.stream().map(RoleMaster::getId).toList());
        return roles.stream().map(role -> toRoleSummaryDto(role, manageableRoleIds.contains(role.getId()),
                userCounts.getOrDefault(role.getId(), 0L))).toList();
    }

    @Override
    @Transactional
    public void deleteRole(String roleUuid, Long requesterId) {
        RoleMaster role = requireRole(roleUuid);
        Long roleId = role.getId();
        validateManageableRole(roleId, requesterId);
        long activeUserCount = countActiveRoleUsers(roleId);
        if (activeUserCount > 0) {
            throw new ValidationException("role.delete.assigned.users", activeUserCount);
        }
        List<Long> reportingRoleIds = roleReportingRoleLinkRepository.findRoleIdsReportingTo(roleId);
        if (!reportingRoleIds.isEmpty()) {
            List<String> reportingRoleNames = repository
                    .findAllByIdInAndIsActiveAndIsDeleted(reportingRoleIds, true, false)
                    .stream()
                    .map(RoleMaster::getName)
                    .toList();
            throw new ValidationException(
                    resolve("role.delete.has.reporting.roles", String.join(", ", reportingRoleNames)));
        }

        role.setIsActive(false);
        role.setIsDeleted(true);
        repository.save(role);
        userRoleLinkRepository.findByRoleId(roleId).forEach(link -> {
            link.setIsActive(false);
            link.setIsDeleted(true);
            userRoleLinkRepository.save(link);
        });
    }

    private void saveReportingRoles(Long roleId, Collection<Long> reportsToRoleIds) {
        roleReportingRoleLinkRepository.deleteByRoleId(roleId);
        if (reportsToRoleIds == null || reportsToRoleIds.isEmpty()) {
            return;
        }

        List<Long> ids = reportsToRoleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<RoleMaster> reportingRoles = repository.findAllByIdInAndIsActiveAndIsDeleted(ids, true, false);
        RoleMaster role = repository.findOneActiveById(roleId);
        if (reportingRoles.size() != ids.size()
                || reportingRoles.stream().anyMatch(parent -> roleId.equals(parent.getId())
                || role == null
                || !Objects.equals(role.getOrgId(), parent.getOrgId()))) {
            throw new ValidationException("role.parent.invalid");
        }
        roleReportingRoleLinkRepository.saveAll(ids.stream()
                .map(reportsToRoleId -> RoleReportingRoleLink.builder()
                        .roleId(roleId)
                        .reportsToRoleId(reportsToRoleId)
                        .build())
                .toList());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void saveModuleAccesses(RoleMaster role, List<RoleModuleAccessDTO> requestedModuleAccess,
                                    Long requesterId) {
        // A missing field means an older client did not intend to edit access. An empty list explicitly clears it.
        if (requestedModuleAccess == null) {
            validateAssignableAccesses(
                    role.getId(), requesterId,
                    roleModuleAccessLinkRepository.findActiveModuleAccessIdsByRoleId(role.getId()));
            return;
        }

        Set<Long> moduleAccessIds = new LinkedHashSet<>();
        for (RoleModuleAccessDTO module : requestedModuleAccess) {
            if (module == null || module.getModuleId() == null || module.getAccesses() == null) {
                continue;
            }
            module.getAccesses().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(access -> {
                        ModuleAccessMaster moduleAccess = moduleAccessMasterRepository
                                .findByModuleIdAndNameAndIsActiveAndIsDeleted(
                                        module.getModuleId(), access, true, false)
                                .orElseThrow(() -> new ValidationException("role.module.access.invalid"));
                        moduleAccessIds.add(moduleAccess.getId());
                    });
        }

        validateAssignableAccesses(role.getId(), requesterId, moduleAccessIds);

        roleModuleAccessLinkRepository.deleteByRoleId(role.getId());
        roleModuleAccessLinkRepository.saveAll(moduleAccessIds.stream()
                .map(moduleAccessId -> RoleModuleAccessLink.builder()
                        .roleId(role.getId())
                        .moduleAccessId(moduleAccessId)
                        .build())
                .toList());
    }

    private void validateAssignableAccesses(Long roleId, Long requesterId, Set<Long> requestedAccessIds) {
        User requester = requesterId == null ? null : userRepository.findOneActiveById(requesterId);
        if (requester == null) {
            throw new ValidationException("role.module.access.denied");
        }
        if (requester.isSuperAdmin() || requestedAccessIds.isEmpty()) {
            return;
        }

        Set<Long> parentRoleIds = findAllParentRoleIds(roleId);
        Set<Long> allowedAccessIds = parentRoleIds.isEmpty()
                ? Collections.emptySet()
                : roleModuleAccessLinkRepository.findActiveModuleAccessIdsByRoleIds(parentRoleIds);
        if (!allowedAccessIds.containsAll(requestedAccessIds)) {
            throw new ValidationException("role.module.access.denied");
        }
    }

    private Set<Long> findAllParentRoleIds(Long roleId) {
        Set<Long> parentRoleIds = new LinkedHashSet<>();
        Set<Long> visitedRoleIds = new LinkedHashSet<>(Set.of(roleId));
        Set<Long> rolesToExpand = new LinkedHashSet<>(Set.of(roleId));

        while (!rolesToExpand.isEmpty()) {
            Set<Long> directParentRoleIds = new LinkedHashSet<>(
                    roleReportingRoleLinkRepository.findActiveParentRoleIds(rolesToExpand));
            directParentRoleIds.removeAll(visitedRoleIds);
            if (directParentRoleIds.isEmpty()) {
                break;
            }
            parentRoleIds.addAll(directParentRoleIds);
            visitedRoleIds.addAll(directParentRoleIds);
            rolesToExpand = directParentRoleIds;
        }

        return parentRoleIds;
    }

    @Transactional(readOnly = true)
    public Set<Long> getManageableRoleIds(Long requesterId) {
        User requester = requesterId == null ? null : userRepository.findOneActiveById(requesterId);
        if (requester == null) {
            throw new ValidationException("role.hierarchy.access.denied");
        }
        Set<Long> assignedRoleIds = new LinkedHashSet<>(
                userRoleLinkRepository.findActiveRoleIdsByUserId(requesterId));
        if (requester.isSuperAdmin()) {
            return repository.findAll().stream()
                    .filter(role -> Boolean.TRUE.equals(role.getIsActive())
                            && !Boolean.TRUE.equals(role.getIsDeleted()))
                    .map(RoleMaster::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        Set<Long> visitedRoleIds = new LinkedHashSet<>(assignedRoleIds);
        Set<Long> rolesToExpand = new LinkedHashSet<>(assignedRoleIds);
        Set<Long> manageableRoleIds = new LinkedHashSet<>();

        while (!rolesToExpand.isEmpty()) {
            Set<Long> childRoleIds = new LinkedHashSet<>(
                    roleReportingRoleLinkRepository.findActiveChildRoleIds(rolesToExpand));
            childRoleIds.removeAll(visitedRoleIds);
            if (childRoleIds.isEmpty()) {
                break;
            }
            manageableRoleIds.addAll(childRoleIds);
            visitedRoleIds.addAll(childRoleIds);
            rolesToExpand = childRoleIds;
        }

        return manageableRoleIds;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getManageableRoleUuids(Long requesterId) {
        Set<Long> ids = getManageableRoleIds(requesterId);
        return repository.findAllById(ids).stream()
                .map(RoleMaster::getUuid)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> getRoleUsers(String roleUuid, Long requesterId) {
        RoleMaster role = requireRole(roleUuid);
        validateOrganizationAccess(role.getOrgId(), requesterId);
        return userRepository.findActiveUsersByRoleId(role.getId()).stream()
                .map(this::toUserSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveRoleUsers(String roleUuid, Long requesterId) {
        RoleMaster role = requireRole(roleUuid);
        validateOrganizationAccess(role.getOrgId(), requesterId);
        return countActiveRoleUsers(role.getId());
    }

    private long countActiveRoleUsers(Long roleId) {
        return userRoleLinkRepository.countActiveUsersByRoleId(roleId);
    }

    private Map<Long, Long> countActiveRoleUsersByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> counts = new HashMap<>();
        userRoleLinkRepository.countActiveUsersByRoleIds(roleIds)
                .forEach(row -> counts.put((Long) row[0], (Long) row[1]));
        return counts;
    }

    private RoleMaster requireRole(String roleUuid) {
        RoleMaster role = roleUuid == null ? null : repository.findOneActiveByUUID(roleUuid);
        if (role == null) {
            throw new ValidationException("role.not.found");
        }
        return role;
    }

    private void validateManageableRole(Long roleId, Long requesterId) {
        if (roleId == null || !getManageableRoleIds(requesterId).contains(roleId)) {
            throw new ValidationException("role.hierarchy.access.denied");
        }
    }

    private void validateOrganizationAccess(Long orgId, Long requesterId) {
        User requester = requesterId == null ? null : userRepository.findOneActiveById(requesterId);
        if (requester == null || orgId == null) {
            throw new ValidationException("role.organization.access.denied");
        }
        if (organizationRepository.findOneActiveById(orgId) == null) {
            throw new ValidationException("role.organization.access.denied");
        }
        if (!requester.isSuperAdmin() && !findAccessibleOrganizationIds(requesterId).contains(orgId)) {
            throw new ValidationException("role.organization.access.denied");
        }
    }

    private Set<Long> findAccessibleOrganizationIds(Long requesterId) {
        Set<Long> accessibleOrganizationIds = new LinkedHashSet<>(
                userOrganizationLinkRepository.findActiveOrganizationIdsByUserId(requesterId));
        Set<Long> organizationsToExpand = new LinkedHashSet<>(accessibleOrganizationIds);
        while (!organizationsToExpand.isEmpty()) {
            Set<Long> childOrganizationIds = new LinkedHashSet<>(
                    organizationRepository.findActiveChildOrganizationIds(organizationsToExpand));
            childOrganizationIds.removeAll(accessibleOrganizationIds);
            if (childOrganizationIds.isEmpty()) {
                break;
            }
            accessibleOrganizationIds.addAll(childOrganizationIds);
            organizationsToExpand = childOrganizationIds;
        }
        return accessibleOrganizationIds;
    }

    private List<User> findActiveOrganizationUsers(Long orgId) {
        List<Long> userIds = userOrganizationLinkRepository.findActiveUserIdsByOrganizationId(orgId);
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userRepository.findAllByIdInAndIsActiveAndIsDeleted(userIds, true, false);
    }

    private void saveRoleUsers(RoleMaster role, RoleDTO roleDTO) {
        boolean wholeCompany = Boolean.TRUE.equals(roleDTO.getWholeCompany());
        if (!wholeCompany && roleDTO.getUserIds() == null) {
            return;
        }

        Set<Long> organizationUserIds = findActiveOrganizationUsers(role.getOrgId()).stream()
                .map(User::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> desiredUserIds = wholeCompany
                ? new LinkedHashSet<>(organizationUserIds)
                : roleDTO.getUserIds().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!organizationUserIds.containsAll(desiredUserIds)) {
            throw new ValidationException("role.user.assignment.invalid");
        }

        List<UserRoleLink> existingLinks = userRoleLinkRepository.findByRoleId(role.getId());
        Map<Long, UserRoleLink> reusableLinks = new HashMap<>();
        existingLinks.forEach(link -> {
            if (desiredUserIds.contains(link.getUserId()) && !reusableLinks.containsKey(link.getUserId())) {
                link.setOrgId(role.getOrgId());
                link.setIsActive(true);
                link.setIsDeleted(false);
                reusableLinks.put(link.getUserId(), link);
            } else {
                link.setIsActive(false);
                link.setIsDeleted(true);
            }
        });
        desiredUserIds.stream()
                .filter(userId -> !reusableLinks.containsKey(userId))
                .forEach(userId -> existingLinks.add(UserRoleLink.builder()
                        .userId(userId)
                        .roleId(role.getId())
                        .orgId(role.getOrgId())
                        .build()));
        userRoleLinkRepository.saveAll(existingLinks);
    }

    private UserSummaryDTO toUserSummaryDto(User user) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .build();
    }

    private RoleSummaryDTO toRoleSummaryDto(RoleMaster role, boolean canManage, long userCount) {
        return RoleSummaryDTO.builder()
                .id(role.getId())
                .uuid(role.getUuid())
                .name(role.getName())
                .orgId(role.getOrgId())
                .userCount(userCount)
                .canManage(canManage)
                .build();
    }

    private RoleDTO toRoleDto(RoleMaster role, boolean canManage, long userCount) {
        return RoleDTO.builder()
                .id(role.getId())
                .uuid(role.getUuid())
                .name(role.getName())
                .orgId(role.getOrgId())
                .reportsToRoleIds(role.getReportsToRoleIds() == null
                        ? roleReportingRoleLinkRepository.findReportsToRoleIds(role.getId())
                        : role.getReportsToRoleIds())
                .moduleAccess(role.getModuleAccess() == null ? getModuleAccess(role.getId()) : role.getModuleAccess())
                .canManage(canManage)
                .build();
    }

    private void validateMyAccessRequest(RoleDTO roleDTO, Long requesterId) {
        if (!Boolean.TRUE.equals(roleDTO.getUseMyAccesses())) {
            return;
        }
        User requester = requesterId == null ? null : userRepository.findOneActiveById(requesterId);
        if (requester == null || !requester.isSuperAdmin()) {
            throw new ValidationException("role.my.accesses.super.admin.only");
        }
    }

    private List<RoleMaster> enrich(List<RoleMaster> roles) {
        roles.forEach(role -> {
            if (role.getId() == null) {
                role.setReportsToRoleIds(Collections.emptyList());
                role.setModuleAccess(Collections.emptyList());
                return;
            }
            role.setReportsToRoleIds(roleReportingRoleLinkRepository.findReportsToRoleIds(role.getId()));
            role.setModuleAccess(getModuleAccess(role.getId()));
        });
        return roles;
    }

    private List<RoleModuleAccessDTO> getModuleAccess(Long roleId) {
        Set<Long> accessIds = roleModuleAccessLinkRepository.findActiveModuleAccessIdsByRoleId(roleId);
        if (accessIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<ModuleAccessMaster>> byModule = moduleAccessMasterRepository.findAllById(accessIds)
                .stream()
                .filter(access -> Boolean.TRUE.equals(access.getIsActive())
                        && !Boolean.TRUE.equals(access.getIsDeleted()))
                .collect(Collectors.groupingBy(ModuleAccessMaster::getModuleId));
        return byModule.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> RoleModuleAccessDTO.builder()
                        .moduleId(entry.getKey())
                        .accesses(entry.getValue().stream()
                                .map(ModuleAccessMaster::getName)
                                .distinct()
                                .sorted(Comparator.comparing(Enum::name))
                                .toList())
                        .build())
                .toList();
    }

    private RoleMaster enrich(RoleMaster role) {
        return role == null ? null : enrich(List.of(role)).getFirst();
    }

}
