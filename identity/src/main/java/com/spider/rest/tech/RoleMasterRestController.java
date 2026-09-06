package com.spider.rest.tech;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.identity.RoleDTO;
import com.spider.common.response.identity.RoleSummaryDTO;
import com.spider.common.response.identity.ModuleDetailDTO;
import com.spider.common.response.identity.UserDTO;
import com.spider.common.response.identity.UserSummaryDTO;
import com.spider.common.rest.BaseResource;
import com.spider.common.exception.ValidationException;
import com.spider.enity.core.User;
import com.spider.enity.tech.RoleMaster;
import com.spider.repository.core.UserRepository;
import com.spider.repository.core.OrganizationRepository;
import com.spider.repository.tech.RoleMasterRepository;
import com.spider.sevice.tech.ModuleMasterService;
import com.spider.sevice.tech.RoleMasterService;
import com.spider.sevice.tech.UserRoleLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/master/role")
@RestController
@Tag(name = "Roles")
@SecurityRequirement(name = "bearerAuth")
public class RoleMasterRestController extends BaseResource {

    private final RoleMasterService roleMasterService;
    private final UserRoleLinkService userRoleLinkService;
    private final ModuleMasterService moduleMasterService;
    private final UserRepository userRepository;
    private final RoleMasterRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    @Autowired
    public RoleMasterRestController(RoleMasterService roleMasterService,
                                    UserRoleLinkService userRoleLinkService,
                                    ModuleMasterService moduleMasterService,
                                    UserRepository userRepository,
                                    RoleMasterRepository roleRepository,
                                    OrganizationRepository organizationRepository) {
        this.roleMasterService = roleMasterService;
        this.userRoleLinkService = userRoleLinkService;
        this.moduleMasterService = moduleMasterService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping(path = "/filter", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Filter roles")
    public CommonResponse<Page<RoleSummaryDTO>> getRoles(@RequestBody RecordFilter filter,
                                                  @RequestAttribute("userId") Long userId) {
        return CommonResponse.of(roleMasterService.filterRoles(filter, userId), resolve("common.success"));
    }

    @GetMapping(path = "/{roleUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a role")
    public CommonResponse<RoleDTO> getRole(@PathVariable String roleUuid,
                                           @RequestAttribute("userId") Long userId) {
        return CommonResponse.of(roleMasterService.getByUuid(roleUuid, userId), resolve("common.success"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a role")
    public CommonResponse<Object> createRole(@RequestBody @Valid RoleDTO request,
                                             @RequestAttribute("userId") Long userId) {
        roleMasterService.saveRole(request, userId);
        return CommonResponse.of(this.resolve("role.create.success"));
    }

    @PutMapping(path = "/{roleUuid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a role")
    public CommonResponse<Object> updateRole(@PathVariable String roleUuid,
                                             @RequestBody @Valid RoleDTO request,
                                             @RequestAttribute("userId") Long userId) {
        roleMasterService.updateRole(roleUuid, request, userId);
        return CommonResponse.of(this.resolve("role.update.success"));
    }

    @GetMapping(path = "/my-accesses", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List the current Super Admin's module accesses")
    public CommonResponse<List<ModuleDetailDTO>> getMyAccesses(
            @RequestAttribute("userId") Long userId) {
        User requester = userRepository.findOneActiveById(userId);
        if (requester == null || !requester.isSuperAdmin()) {
            throw new ValidationException("role.my.accesses.super.admin.only");
        }
        return CommonResponse.of(moduleMasterService.getAccessibleModules(userId), resolve("common.success"));
    }

    @PostMapping(path = "/assign/{userUuid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Assign roles to a user")
    public CommonResponse<Object> assignRoles(@PathVariable String userUuid, @RequestBody List<String> roleUuids) {
        User target = requireUser(userUuid);
        List<Long> roleIds = roleUuids.stream().map(this::requireRole).map(RoleMaster::getId).toList();
        roleMasterService.assignRole(target.getId(), roleIds);
        return CommonResponse.of(this.resolve("role.assign.success"));
    }

    @GetMapping(path = "/module-access/{moduleAccessId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List roles for module access")
    public CommonResponse<List<RoleSummaryDTO>> getByModuleAccess(@PathVariable("moduleAccessId") Long moduleAccessId,
                                                           @RequestAttribute("userId") Long userId) {
        return CommonResponse.of(roleMasterService.getByModuleAccessDtos(moduleAccessId, userId), resolve("common.success"));
    }

    @PutMapping(path = "/{roleUuid}/org/{orgUuid}/user/{userUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Assign a role to a user in an organization")
    public UserDTO replaceRole(@PathVariable String roleUuid, @PathVariable String orgUuid,
                               @PathVariable String userUuid) {
        return userRoleLinkService.assignUserOrgRole(
                requireUser(userUuid).getId(), requireRole(roleUuid).getId(), requireOrganizationId(orgUuid));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all roles")
    public CommonResponse<List<RoleSummaryDTO>> getAllRoles(@RequestAttribute("userId") Long userId) {
        return CommonResponse.of(roleMasterService.getAllRoles(userId), resolve("common.success"));
    }

    @DeleteMapping(path = "/{roleUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a role")
    public CommonResponse<Object> deleteRole(@PathVariable String roleUuid,
                                             @RequestAttribute("userId") Long userId) {
        roleMasterService.deleteRole(roleUuid, userId);
        return CommonResponse.of(resolve("role.delete.success"));
    }

    @GetMapping(path = "/manageable-uuids", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List roles the current user may edit or delete")
    public CommonResponse<java.util.Set<String>> getManageableRoleIds(
            @RequestAttribute("userId") Long userId) {
        return CommonResponse.of(roleMasterService.getManageableRoleUuids(userId), resolve("common.success"));
    }

    @GetMapping(path = "/{roleUuid}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List active users assigned to a role")
    public CommonResponse<List<UserSummaryDTO>> getRoleUsers(
            @PathVariable String roleUuid, @RequestAttribute("userId") Long userId) {
        return CommonResponse.of(roleMasterService.getRoleUsers(roleUuid, userId), resolve("common.success"));
    }

    @GetMapping(path = "/{roleUuid}/active-user-count", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Count active users assigned to a role")
    public CommonResponse<Long> countActiveRoleUsers(@PathVariable String roleUuid,
                                                     @RequestAttribute("userId") Long userId) {
        return CommonResponse.of(roleMasterService.countActiveRoleUsers(roleUuid, userId), resolve("common.success"));
    }

    @DeleteMapping(path = "/org/{orgUuid}/user/{userUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove a user's organization role")
    public CommonResponse<UserDTO> removeRole(@PathVariable String orgUuid, @PathVariable String userUuid) {
        return CommonResponse.of(userRoleLinkService.revokeUserOrgRole(
                requireUser(userUuid).getId(), requireOrganizationId(orgUuid)), resolve("common.success"));
    }

    private User requireUser(String uuid) {
        User user = userRepository.findOneActiveByUUID(uuid);
        if (user == null) throw new ValidationException("user.not.found");
        return user;
    }

    private RoleMaster requireRole(String uuid) {
        RoleMaster role = roleRepository.findOneActiveByUUID(uuid);
        if (role == null) throw new ValidationException("role.not.found");
        return role;
    }

    private Long requireOrganizationId(String uuid) {
        var organization = organizationRepository.findOneActiveByUUID(uuid);
        if (organization == null) throw new ValidationException("organization.not.found");
        return organization.getId();
    }

}
