package com.spider.sevice.core.impl;


import com.spider.common.repository.ParentRepository;
import com.spider.common.dto.UserSessionDTO;
import com.spider.common.exception.ValidationException;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.UserCreateRequest;
import com.spider.common.request.identity.UserUpdateRequest;
import com.spider.common.response.identity.UserDTO;
import com.spider.common.response.identity.UserSummaryDTO;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.core.Location;
import com.spider.enity.core.Organization;
import com.spider.enity.core.User;
import com.spider.enity.tech.RoleMaster;
import com.spider.repository.core.LocationRepository;
import com.spider.repository.core.UserLocationLinkRepository;
import com.spider.repository.core.OrganizationRepository;
import com.spider.repository.core.UserOrganizationLinkRepository;
import com.spider.repository.core.UserRepository;
import com.spider.repository.tech.UserRoleLinkRepository;
import com.spider.repository.tech.RoleMasterRepository;
import com.spider.repository.tech.RoleReportingRoleLinkRepository;
import com.spider.sevice.core.UserService;
import com.spider.sevice.core.UserOrganizationLinkService;
import com.spider.sevice.core.UserLocationLinkService;
import com.spider.sevice.core.UserReportingUserLinkService;
import com.spider.sevice.tech.UserRoleLinkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Log4j2
@Service
public class UserServiceImpl extends CommonServiceImpl<User,Long> implements UserService {

    private final UserRepository userRepository;
    private final CriteriaUtil<User> criteriaUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserOrganizationLinkService userOrganizationLinkService;
    private final UserLocationLinkService userLocationLinkService;
    private final UserRoleLinkService userRoleLinkService;
    private final UserOrganizationLinkRepository userOrganizationLinkRepository;
    private final UserLocationLinkRepository userLocationLinkRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final OrganizationRepository organizationRepository;
    private final LocationRepository locationRepository;
    private final RoleMasterRepository roleMasterRepository;
    private final RoleReportingRoleLinkRepository roleReportingRoleLinkRepository;
    private final UserReportingUserLinkService userReportingUserLinkService;
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           CriteriaUtil<User> criteriaUtil,
                           BCryptPasswordEncoder passwordEncoder,
                           UserOrganizationLinkService userOrganizationLinkService,
                           UserLocationLinkService userLocationLinkService,
                           UserRoleLinkService userRoleLinkService,
                           UserOrganizationLinkRepository userOrganizationLinkRepository,
                           UserLocationLinkRepository userLocationLinkRepository,
                           UserRoleLinkRepository userRoleLinkRepository,
                           OrganizationRepository organizationRepository,
                           LocationRepository locationRepository,
                           RoleMasterRepository roleMasterRepository,
                           RoleReportingRoleLinkRepository roleReportingRoleLinkRepository,
                           UserReportingUserLinkService userReportingUserLinkService) {
        this.userRepository = userRepository;
        this.criteriaUtil = criteriaUtil;
        this.passwordEncoder = passwordEncoder;
        this.userOrganizationLinkService = userOrganizationLinkService;
        this.userLocationLinkService = userLocationLinkService;
        this.userRoleLinkService = userRoleLinkService;
        this.userOrganizationLinkRepository = userOrganizationLinkRepository;
        this.userLocationLinkRepository = userLocationLinkRepository;
        this.userRoleLinkRepository = userRoleLinkRepository;
        this.organizationRepository = organizationRepository;
        this.locationRepository = locationRepository;
        this.roleMasterRepository = roleMasterRepository;
        this.roleReportingRoleLinkRepository = roleReportingRoleLinkRepository;
        this.userReportingUserLinkService = userReportingUserLinkService;
    }


    @Override
    protected ParentRepository<User, Long> getRepository() {
        return userRepository;
    }

    @Override
    protected CriteriaUtil<User> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    protected void addEntityDataSecurityCheck(RecordFilter recordFilter, UserSessionDTO userSession) {
        if (userSession == null || userSession.getUserId() == null
                || userRepository.findOneActiveById(userSession.getUserId()) == null) {
            appendAllowedUserIds(recordFilter, Collections.emptySet());
            return;
        }

        appendAllowedUserIds(recordFilter, findVisibleUserIds(userSession.getUserId()));
    }

    private void appendAllowedUserIds(RecordFilter recordFilter, Set<Long> accessibleUserIds) {
        List<String> values = accessibleUserIds.isEmpty()
                ? List.of("-1")
                : accessibleUserIds.stream().sorted().map(String::valueOf).toList();
        recordFilter.appendCriteria("id", "in", values);
    }

    private Set<Long> findVisibleUserIds(Long requesterId) {
        Set<Long> organizationIds = findAccessibleOrganizationIds(requesterId);
        Set<Long> visibleUserIds = organizationIds.isEmpty()
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(userOrganizationLinkRepository
                        .findActiveUserIdsByOrganizationIds(organizationIds));

        Set<Long> roleIds = findSameAndLowerRoleIds(requesterId);
        if (!roleIds.isEmpty()) {
            visibleUserIds.retainAll(userRoleLinkRepository.getUserIdsByRoleId(roleIds));
        }

        Set<Long> locationIds = userLocationLinkRepository.findActiveLocationIdsByUserId(requesterId);
        if (locationIds.isEmpty()) {
            visibleUserIds.clear();
        } else {
            visibleUserIds.retainAll(userLocationLinkRepository.findActiveUserIdsByLocationIds(locationIds));
        }

        // A user can always see their own row, even when their account is not linked
        // to an organization or location yet.
        visibleUserIds.add(requesterId);
        return visibleUserIds;
    }

    private Set<Long> findAccessibleOrganizationIds(Long requesterId) {
        Set<Long> organizationIds = new LinkedHashSet<>(
                userOrganizationLinkRepository.findActiveOrganizationIdsByUserId(requesterId));
        Set<Long> toExpand = new LinkedHashSet<>(organizationIds);
        while (!toExpand.isEmpty()) {
            Set<Long> children = new LinkedHashSet<>(
                    organizationRepository.findActiveChildOrganizationIds(toExpand));
            children.removeAll(organizationIds);
            if (children.isEmpty()) {
                break;
            }
            organizationIds.addAll(children);
            toExpand = children;
        }
        return organizationIds;
    }

    private Set<Long> findSameAndLowerRoleIds(Long requesterId) {
        Set<Long> roleIds = new LinkedHashSet<>(
                userRoleLinkRepository.findActiveRoleIdsByUserId(requesterId));
        Set<Long> toExpand = new LinkedHashSet<>(roleIds);
        while (!toExpand.isEmpty()) {
            Set<Long> children = new LinkedHashSet<>(
                    roleReportingRoleLinkRepository.findActiveChildRoleIds(toExpand));
            children.removeAll(roleIds);
            if (children.isEmpty()) {
                break;
            }
            roleIds.addAll(children);
            toExpand = children;
        }
        return roleIds;
    }

    @Override
    public UserDTO getById(Long userId) {
        UserDTO userDTO = toDto(get(userId));
        if (userDTO == null) {
            return null;
        }
        userDTO.setOrgIdList(userOrganizationLinkRepository.findActiveOrganizationIdsByUserId(userId).stream().toList());
        userDTO.setLocationIdList(userLocationLinkRepository.findActiveLocationIdsByUserId(userId).stream().toList());
        userDTO.setRoleIdList(userRoleLinkRepository.findActiveRoleIdsByUserId(userId).stream().toList());
        userDTO.setReportsToUserIdList(userReportingUserLinkService.getReportsToUserIds(userId).stream().toList());
        userDTO.setCanManage(canCurrentUserManage(userId));
        return userDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getVisibleByUuid(String userUuid) {
        User user = requireUser(userUuid);
        // This detail endpoint supplies the edit form, so validate the reporting
        // hierarchy when Edit is clicked. The user list remains visible to everyone
        // in the same organization.
        validateManageAccess(user.getId());
        return getById(user.getId());
    }

    @Override
    public UserDTO buildUserDetails(Long userId, boolean b, boolean b1) {
        return toDto(get(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDTO> getAllUsers(RecordFilter filter) {
        UserSessionDTO session = getCurrentUserSession();
        Set<Long> manageableUserIds = session == null || session.getUserId() == null
                ? Collections.emptySet()
                : findAllDirectAndIndirectReports(session.getUserId());
        boolean manageAll = session != null && session.isSuperAdmin();
        Long requesterId = session == null ? null : session.getUserId();
        return filter(filter).map(user -> toSummaryDto(user,
                requesterId != null
                        && (manageAll || manageableUserIds.contains(user.getId()))));
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        validateRequestedAccess(request.getOrgIdList(), request.getLocationIdList(), request.getRoleIdList());
        validateEmailAvailable(request.getEmail(), null);
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFname(request.getFname());
        user.setLname(request.getLname());
        user.setFullName(buildFullName(request.getFname(), request.getLname()));
        user.setUsername(request.getUsername());
        user.setEmailVerified(false);
        user.setOtpVerified(false);
        user.setStatusId(request.getStatusId());
        user.setSuperAdmin(false);
        User savedUser = saveUser(user, request.getEmail());
        userOrganizationLinkService.replaceLinks(savedUser.getId(), request.getOrgIdList());
        userLocationLinkService.replaceLinks(savedUser.getId(), request.getLocationIdList());
        userRoleLinkService.replaceLinks(savedUser.getId(), request.getRoleIdList());
        replaceReportingLinks(savedUser.getId(), request.getReportsToUserIdList(), true);
        return getById(savedUser.getId());
    }

    @Override
    @Transactional
    public UserDTO updateUser(String userUuid, UserUpdateRequest request) {
        User user = requireUser(userUuid);
        Long userId = user.getId();
        validateManageAccess(userId);
        validateRequestedAccess(request.getOrgIdList(), request.getLocationIdList(), request.getRoleIdList());
        validateEmailAvailable(request.getEmail(), userId);
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setFname(request.getFname());
        user.setLname(request.getLname());
        user.setFullName(buildFullName(request.getFname(), request.getLname()));
        user.setUsername(request.getUsername());
//        user.setEmailVerified(request.getEmailVerified());
//        user.setOtpVerified(request.getOtpVerified());
        user.setStatusId(request.getStatusId());
        User savedUser = saveUser(user, request.getEmail());
        userOrganizationLinkService.replaceLinks(savedUser.getId(), request.getOrgIdList());
        userLocationLinkService.replaceLinks(savedUser.getId(), request.getLocationIdList());
        userRoleLinkService.replaceLinks(savedUser.getId(), request.getRoleIdList());
        replaceReportingLinks(savedUser.getId(), request.getReportsToUserIdList(), true);
        return getById(savedUser.getId());
    }

    @Override
    @Transactional
    public void deleteUser(String userUuid) {
        deleteUserById(requireUser(userUuid).getId());
    }

    private void deleteUserById(Long userId) {
        validateManageAccess(userId);
        User user = get(userId);
        user.setIsActive(false);
        user.setIsDeleted(true);
        userRepository.save(user);
        userOrganizationLinkService.deactivateByUserId(userId);
        userReportingUserLinkService.deactivateLinksForUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        return toDto(userRepository.findByEmail(email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByEmails(List<String> emails) {
        return userRepository.findByEmailIn(emails).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public UserDTO updateUserEmail(Long userId, String email) {
        validateEmailAvailable(email, userId);
        User user = get(userId);
        user.setEmail(email);
        user.setEmailVerified(false);
        return toDto(saveUser(user, email));
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = get(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public List<UserDTO> createUsers(List<UserCreateRequest> requests) {
        return requests.stream().map(this::createUser).toList();
    }

    @Override
    @Transactional
    public void deleteUsers(List<Long> userIds) {
        userIds.forEach(this::deleteUserById);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDTO> getOrganizationUsers(String organizationUuid, RecordFilter filter) {
        Organization organization = organizationRepository.findOneActiveByUUID(organizationUuid);
        if (organization == null) throw new ValidationException("organization.not.found");
        filter.appendCriteria("id", "in",
                userOrganizationLinkService.getActiveUserIdsByOrganizationId(organization.getId()));
        return getAllUsers(filter);
    }



    private UserDTO toDto(User user) {
        return toDto(user, user != null && canCurrentUserManage(user.getId()));
    }

    private UserDTO toDto(User user, boolean canManage) {
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
                .orgIdList(userOrganizationLinkRepository.findActiveOrganizationIdsByUserId(user.getId()).stream().toList())
                .locationIdList(userLocationLinkRepository.findActiveLocationIdsByUserId(user.getId()).stream().toList())
                .roleIdList(userRoleLinkRepository.findActiveRoleIdsByUserId(user.getId()).stream().toList())
                .reportsToUserIdList(userReportingUserLinkService.getReportsToUserIds(user.getId()).stream().toList())
                .canManage(canManage)
                .build();
    }

    private UserSummaryDTO toSummaryDto(User user, boolean canManage) {
        return UserSummaryDTO.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .orgIdList(userOrganizationLinkRepository
                        .findActiveOrganizationIdsByUserId(user.getId()).stream().toList())
                .canManage(canManage)
                .build();
    }

    private String buildFullName(String firstName, String lastName) {
        String normalizedFirstName = firstName == null ? "" : firstName.trim();
        String normalizedLastName = lastName == null ? "" : lastName.trim();
        return normalizedLastName.isEmpty()
                ? normalizedFirstName
                : normalizedFirstName + " " + normalizedLastName;
    }

    private void validateEmailAvailable(String email, Long excludedUserId) {
        boolean exists = excludedUserId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, excludedUserId);
        if (exists) {
            throw new ValidationException("user.email.exists", email);
        }
    }

    private User saveUser(User user, String email) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            if (hasConstraint(exception, "m_user_email_key")) {
                throw new ValidationException("user.email.exists", email);
            }
            throw exception;
        }
    }

    private boolean hasConstraint(Throwable throwable, String constraintName) {
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(constraintName)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void validateRequestedAccess(List<Long> orgIds, List<Long> locationIds, List<Long> roleIds) {
        UserSessionDTO session = getCurrentUserSession();
        if (session == null || session.getUserId() == null) {
            throw new ValidationException("user.access.denied");
        }
        Long requesterId = session.getUserId();

        User requester = userRepository.findOneActiveById(requesterId);
        if (requester == null) {
            throw new ValidationException("user.access.denied");
        }
        if (requester.isSuperAdmin()) {
            return;
        }

        validateAccess("orgIdList", resolveAccessItems(orgIds,
                        organizationRepository::findAllById, Organization::getId, Organization::getAppName),
                findAccessibleOrganizationIds(requesterId));
        validateAccess("locationIdList", resolveAccessItems(locationIds,
                        locationRepository::findAllById, Location::getId, Location::getLocationName),
                userLocationLinkRepository.findActiveLocationIdsByUserId(requesterId));
        validateAccess("roleIdList", resolveAccessItems(roleIds,
                        roleMasterRepository::findAllById, RoleMaster::getId, RoleMaster::getName),
                findSameAndLowerRoleIds(requesterId));
    }

    private UserSessionDTO getCurrentUserSession() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        Object session = attributes.getRequest().getAttribute("userSession");
        if (session instanceof UserSessionDTO userSession) {
            return userSession;
        }
        Object userId = attributes.getRequest().getAttribute("userId");
        if (userId instanceof Long requesterId) {
            UserSessionDTO fallback = new UserSessionDTO();
            fallback.setUserId(requesterId);
            User requester = userRepository.findOneActiveById(requesterId);
            fallback.setSuperAdmin(requester != null && requester.isSuperAdmin());
            return fallback;
        }
        return null;
    }

    private boolean canCurrentUserManage(Long targetUserId) {
        UserSessionDTO session = getCurrentUserSession();
        if (session == null || session.getUserId() == null) {
            return false;
        }
        if (session.isSuperAdmin()) {
            return true;
        }
        return !session.getUserId().equals(targetUserId)
                && findAllDirectAndIndirectReports(session.getUserId()).contains(targetUserId);
    }

    private void validateManageAccess(Long targetUserId) {
        if (userRepository.findOneActiveById(targetUserId) == null) {
            throw new ValidationException("user.not.found");
        }
        UserSessionDTO session = getCurrentUserSession();
        if (session != null && !session.isSuperAdmin() && session.getUserId() != null
                && session.getUserId().equals(targetUserId)) {
            throw new ValidationException("user.self.manage.denied");
        }
        if (!canCurrentUserManage(targetUserId)) {
            throw new ValidationException("user.hierarchy.access.denied");
        }
    }

    private Set<Long> findAllDirectAndIndirectReports(Long managerId) {
        Set<Long> reports = new LinkedHashSet<>();
        Set<Long> managersToExpand = new LinkedHashSet<>(Set.of(managerId));
        while (!managersToExpand.isEmpty()) {
            Set<Long> directReports = new LinkedHashSet<>(
                    userReportingUserLinkService.getDirectReportUserIds(managersToExpand));
            directReports.removeAll(reports);
            directReports.remove(managerId);
            if (directReports.isEmpty()) {
                break;
            }
            reports.addAll(directReports);
            managersToExpand = directReports;
        }
        return reports;
    }

    private void replaceReportingLinks(Long userId, List<Long> reportsToUserIds,
                                       boolean allowRequesterOutsideOrganization) {
        Set<Long> requestedIds = reportsToUserIds == null
                ? Collections.emptySet()
                : reportsToUserIds.stream()
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        validateReportingUsers(userId, requestedIds, allowRequesterOutsideOrganization);

        userReportingUserLinkService.replaceLinks(userId, requestedIds);
    }

    private void validateReportingUsers(Long userId, Set<Long> managerIds,
                                        boolean allowRequesterOutsideOrganization) {
        if (managerIds.isEmpty()) {
            return;
        }
        if (managerIds.contains(userId)
                || managerIds.stream().anyMatch(findAllDirectAndIndirectReports(userId)::contains)) {
            throw new ValidationException("user.reporting.invalid");
        }

        Set<Long> userOrganizationIds = userOrganizationLinkRepository
                .findActiveOrganizationIdsByUserId(userId);
        Map<Long, User> activeManagers = userRepository.findAllById(managerIds).stream()
                .filter(manager -> Boolean.TRUE.equals(manager.getIsActive())
                        && !Boolean.TRUE.equals(manager.getIsDeleted()))
                .collect(java.util.stream.Collectors.toMap(User::getId, Function.identity()));
        if (activeManagers.size() != managerIds.size()) {
            throw new ValidationException("user.reporting.invalid");
        }
        UserSessionDTO session = getCurrentUserSession();
        Long requesterId = session == null ? null : session.getUserId();
        Set<Long> existingManagerIds = userReportingUserLinkService.getReportsToUserIds(userId);
        boolean invalidOrganization = managerIds.stream().anyMatch(managerId -> {
            if (existingManagerIds.contains(managerId)
                    || (allowRequesterOutsideOrganization && managerId.equals(requesterId))) {
                return false;
            }
            Set<Long> managerOrganizationIds = userOrganizationLinkRepository
                    .findActiveOrganizationIdsByUserId(managerId);
            return Collections.disjoint(userOrganizationIds, managerOrganizationIds);
        });
        if (invalidOrganization) {
            throw new ValidationException("user.reporting.invalid");
        }
    }

    private void validateAccess(String fieldName, List<AccessItem> requestedItems, Set<Long> accessibleIds) {
        List<String> inaccessibleNames = requestedItems.stream()
                .filter(item -> !accessibleIds.contains(item.id()))
                .map(AccessItem::name)
                .toList();
        if (!inaccessibleNames.isEmpty()) {
            throw new ValidationException(
                    "user.access.denied.for.field", fieldName, inaccessibleNames);
        }
    }

    private <T> List<AccessItem> resolveAccessItems(
            List<Long> requestedIds,
            Function<List<Long>, List<T>> entityLoader,
            Function<T, Long> idExtractor,
            Function<T, String> nameExtractor) {
        if (requestedIds == null) {
            return Collections.emptyList();
        }

        List<Long> uniqueIds = requestedIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> namesById = new LinkedHashMap<>();
        entityLoader.apply(uniqueIds).forEach(entity ->
                namesById.put(idExtractor.apply(entity), nameExtractor.apply(entity)));

        return uniqueIds.stream()
                .map(id -> {
                    String name = namesById.get(id);
                    return new AccessItem(id,
                            name == null || name.isBlank() ? "Unknown (ID: " + id + ")" : name);
                })
                .toList();
    }

    private record AccessItem(Long id, String name) {
    }

    private User requireUser(String userUuid) {
        User user = userUuid == null ? null : userRepository.findOneActiveByUUID(userUuid);
        if (user == null) {
            throw new ValidationException("user.not.found");
        }
        return user;
    }
}
