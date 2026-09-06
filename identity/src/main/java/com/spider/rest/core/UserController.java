package com.spider.rest.core;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.UserCreateRequest;
import com.spider.common.request.identity.UserUpdateRequest;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.identity.UserDTO;
import com.spider.common.response.identity.UserSummaryDTO;
import com.spider.common.rest.BaseResource;
import com.spider.sevice.core.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/master/user")
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController extends BaseResource {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/filter", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Filter users")
    public CommonResponse<Page<UserSummaryDTO>> filter(@RequestBody RecordFilter filter) {
        return CommonResponse.of(userService.getAllUsers(filter), resolve("common.success"));
    }

    @PostMapping(path = "/organization/{organizationUuid}/filter", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Filter visible users in an organization")
    public CommonResponse<Page<UserSummaryDTO>> filterByOrganization(
            @PathVariable String organizationUuid, @RequestBody RecordFilter filter) {
        return CommonResponse.of(userService.getOrganizationUsers(organizationUuid, filter), resolve("common.success"));
    }

    @GetMapping(path = "/{userUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a user")
    public CommonResponse<UserDTO> get(@PathVariable String userUuid) {
        return CommonResponse.of(userService.getVisibleByUuid(userUuid), resolve("common.success"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a user")
    public CommonResponse<UserDTO> create(@RequestBody @Valid UserCreateRequest request) {
        return CommonResponse.of(userService.createUser(request), resolve("user.create.success"));
    }

    @PutMapping(path = "/{userUuid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a user")
    public CommonResponse<UserDTO> update(@PathVariable String userUuid,
                                          @RequestBody @Valid UserUpdateRequest request) {
        return CommonResponse.of(userService.updateUser(userUuid, request), resolve("user.update.success"));
    }

    @DeleteMapping("/{userUuid}")
    @Operation(summary = "Deactivate a user")
    public CommonResponse<Object> delete(@PathVariable String userUuid) {
        userService.deleteUser(userUuid);
        return CommonResponse.of(resolve("user.delete.success"));
    }
}
