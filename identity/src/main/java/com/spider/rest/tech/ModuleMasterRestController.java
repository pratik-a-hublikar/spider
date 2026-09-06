package com.spider.rest.tech;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.ModuleRequest;
import com.spider.common.request.identity.ModuleUpdateRequest;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.identity.ModuleManagementDTO;
import com.spider.common.response.identity.ModuleDetailDTO;
import com.spider.common.rest.BaseResource;
import com.spider.enity.tech.ModuleMaster;
import com.spider.sevice.tech.ModuleMasterService;
import com.spider.sevice.tech.ModuleAccessMasterService;
import com.spider.sevice.tech.RoleMasterService;
import com.spider.enity.tech.ModuleAccessMaster;
import com.spider.enity.tech.RoleMaster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/module")
@RestController
@Tag(name = "Modules")
@SecurityRequirement(name = "bearerAuth")
public class ModuleMasterRestController extends BaseResource {

    private final ModuleMasterService moduleService;
    private final ModuleAccessMasterService moduleAccessService;
    private final RoleMasterService roleService;

    @Autowired
    public ModuleMasterRestController(ModuleMasterService moduleService,
                                      ModuleAccessMasterService moduleAccessService,
                                      RoleMasterService roleService) {
        this.moduleService = moduleService;
        this.moduleAccessService = moduleAccessService;
        this.roleService = roleService;
    }

    @GetMapping(path = "/accessible", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List modules accessible to the current user")
    public CommonResponse<List<ModuleDetailDTO>> getAccessibleModules(
            @RequestAttribute(name = "userId", required = false) Long userId) {
        return CommonResponse.of(this.moduleService.getAccessibleModules(userId), resolve("common.success"));
    }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a module")
    public CommonResponse<Object> createModule(@RequestBody @Valid ModuleRequest request) {
        this.moduleService.createModule(request);
        return CommonResponse.of(this.resolve("module.create.success"));
    }

    @PostMapping(path = "/filter", produces = MediaType.APPLICATION_JSON_VALUE, consumes= MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Filter modules")
    public CommonResponse<Page<ModuleMaster>> getModule(@RequestBody RecordFilter filter) {
        return CommonResponse.of(this.moduleService.getAllModule(filter), resolve("common.success"));
    }

    @GetMapping(path = "/{moduleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get module details")
    public CommonResponse<ModuleManagementDTO> getModuleDetailByModuleId(@PathVariable("moduleId") Long moduleId) {
        return CommonResponse.of(this.moduleService.getModuleById(moduleId), resolve("common.success"));
    }



    @PutMapping(path = "/{moduleId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a module")
    public CommonResponse<Object> updateModule(@RequestBody @Valid ModuleUpdateRequest request,
                                               @RequestAttribute("userId") Long userId) {
        this.moduleService.updateModule(request,userId);
        return CommonResponse.of(this.resolve("module.update.success"));
    }

    @DeleteMapping(path = "/{moduleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a module")
    public CommonResponse<Object> deleteModuleById(@PathVariable("moduleId") Long id) {
        ModuleMaster moduleList = this.moduleService.get(id);
        if (!moduleList.isMasterEntry() && CollectionUtils.isEmpty(moduleList.getChildModules())) {
            this.moduleService.deleteModuleById(id);
        }
        if(moduleList.isMasterEntry()){
            return CommonResponse.of(this.resolve("module.delete.fail.master.entry"));
        }

        return CommonResponse.of(this.resolve(CollectionUtils.isEmpty(moduleList.getChildModules()) ? "module.delete.success" : "module.delete.fail"));
    }

    @GetMapping(path = "/parent", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List parent modules")
    public CommonResponse<List<ModuleMaster>> getParentModules() {
        return CommonResponse.of(moduleService.getParentModuleList(), resolve("common.success"));
    }

    @GetMapping(path = "/{moduleId}/children", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List child modules")
    public CommonResponse<List<ModuleMaster>> getChildModules(@PathVariable Long moduleId) {
        return CommonResponse.of(moduleService.getChildModule(moduleId), resolve("common.success"));
    }

    @GetMapping(path = "/{moduleId}/access", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List access rules for a module")
    public CommonResponse<List<ModuleAccessMaster>> getModuleAccess(@PathVariable Long moduleId) {
        return CommonResponse.of(moduleAccessService.getByModuleId(moduleId), resolve("common.success"));
    }

    @GetMapping(path = "/role/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get modules assigned to a role")
    public CommonResponse<Map<String, Object>> getRoleModules(@PathVariable Long roleId) {
        RoleMaster role = roleService.get(roleId);
        return CommonResponse.of(Map.of(
                "module", moduleService.getRoleModule(List.of(roleId)),
                "role", role));
    }

    @GetMapping(path = "/app/{appId}/org/{orgId}/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get modules assigned to a user")
    public CommonResponse<List<ModuleDetailDTO>> getUserModules(@PathVariable Long appId,
                                                                 @PathVariable Long orgId,
                                                                 @PathVariable Long userId) {
        return CommonResponse.of(moduleService.getUserModules(userId, appId, orgId), resolve("common.success"));
    }

}
