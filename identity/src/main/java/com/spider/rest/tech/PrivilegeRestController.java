package com.spider.rest.tech;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.PrivilegeCreateRequest;
import com.spider.common.request.identity.PrivilegeUpdateRequest;
import com.spider.common.response.CommonResponse;
import com.spider.common.rest.BaseResource;
import com.spider.dto.PrivilegeDTO;
import com.spider.enity.tech.PrivilegeMaster;
import com.spider.sevice.tech.PrivilegeMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/privilege")
@RestController
@Tag(name = "Privileges")
@SecurityRequirement(name = "bearerAuth")
public class PrivilegeRestController extends BaseResource {

    private final PrivilegeMasterService privilegeMasterService;

    @Autowired
    public PrivilegeRestController(PrivilegeMasterService privilegeMasterService) {
        this.privilegeMasterService = privilegeMasterService;
    }

    @PostMapping(path = "/filter", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Filter privileges")
    public CommonResponse<Page<PrivilegeMaster>> getPrivilege(@RequestBody RecordFilter filter) {
        return CommonResponse.of(privilegeMasterService.getAllPrivilege(filter), resolve("common.success"));
    }
    @GetMapping(path = "/{privilegeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a privilege")
    public CommonResponse<PrivilegeDTO> getPrivilegeByPrivilegeId(@PathVariable("privilegeId") Long privilegeId) {
        return CommonResponse.of(privilegeMasterService.getPrivilegeById(privilegeId), resolve("common.success"));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a privilege")
    public CommonResponse<Object> updatePrivilege(@RequestBody @Valid PrivilegeUpdateRequest request) {
        privilegeMasterService.updatePrivilege(request);
        return CommonResponse.of(this.resolve("privilege.update.success"));
    }

    @DeleteMapping(path = "/{privilegeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a privilege")
    public CommonResponse<Object> deletePrivilegeById(@PathVariable("privilegeId") Long id) {
        privilegeMasterService.deletePrivilegeByPrivilegeId(id);
        return CommonResponse.of(this.resolve("privilege.delete.success"));
    }

    @GetMapping(path = "/permission", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all permissions")
    public List<PrivilegeDTO> getAllPermissions() {
        return privilegeMasterService.getAllPermissions();
    }

    @PostMapping( consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a privilege")
    public CommonResponse<Object> createPrivilege(@RequestBody @Valid PrivilegeCreateRequest request) {
        privilegeMasterService.createPrivilege(request);
        return CommonResponse.of(this.resolve("privilege.create.success"));
    }

}
