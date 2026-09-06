package com.spider.rest.core;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.LocationRequest;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.identity.LocationDTO;
import com.spider.common.response.identity.LocationUserDTO;
import com.spider.common.rest.BaseResource;
import com.spider.enity.core.Location;
import com.spider.sevice.core.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import io.vavr.Tuple2;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/master/location")
@Tag(name = "Locations")
@SecurityRequirement(name = "bearerAuth")
public class LocationController extends BaseResource {
    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter locations")
    public CommonResponse<Page<LocationDTO>> filter(@RequestBody RecordFilter filter) {
        Page<Location> locations = service.filter(filter);
        Map<Long, Long> userCounts = service.countActiveUsersByLocationIds(
                locations.getContent().stream().map(Location::getId).toList());
        return CommonResponse.of(locations.map(location -> toDto(location, userCounts.getOrDefault(location.getId(), 0L))),
                resolve("common.success"));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get a location")
    public CommonResponse<LocationDTO> get(@PathVariable String uuid) {
        return CommonResponse.of(toDto(service.getByUuid(uuid)), resolve("common.success"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a location")
    public CommonResponse<LocationDTO> create(@RequestBody @Valid LocationRequest request,
                                              @RequestAttribute("userId") Long userId) {
        Location entity = service.createLocation(request,userId);
        return CommonResponse.of(toDto(entity), resolve("location.create.success"));
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update a location")
    public CommonResponse<LocationDTO> update(@PathVariable String uuid,
                                               @RequestBody @Valid LocationRequest request) {
        Tuple2<Location, Boolean> result = service.updateLocation(uuid, request);
        return CommonResponse.of(toDto(result._1()),
                result._2() ? resolve("location.update.success") : resolve("location.update.fail"));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Deactivate a location")
    public CommonResponse<Object> delete(@PathVariable String uuid) {
        service.softDelete(uuid);
        return CommonResponse.of(resolve("location.delete.success"));
    }

    @GetMapping("/users")
    @Operation(summary = "Get active users assigned to a location")
    public CommonResponse<List<LocationUserDTO>> users(@RequestParam String locationUuid,
                                                       @RequestAttribute("userId") Long requesterId) {
        return CommonResponse.of(service.getLocationUsers(locationUuid, requesterId), resolve("common.success"));
    }

    @DeleteMapping("/user")
    @Operation(summary = "Remove a location assignment from a user")
    public CommonResponse<Object> removeUser(@RequestParam String locationUuid,
                                             @RequestParam String userUuid,
                                             @RequestAttribute("userId") Long requesterId) {
        service.removeUserLocation(locationUuid, userUuid, requesterId);
        return CommonResponse.of(resolve("user.location.remove.success"));
    }

    private LocationDTO toDto(Location entity) {
        return toDto(entity, 0L);
    }

    private LocationDTO toDto(Location entity, Long userCount) {
        if (entity == null) return null;
        return LocationDTO.builder().id(entity.getId()).uuid(entity.getUuid()).organizationId(entity.getOrgId())
                .organizationName(entity.getOrganization() != null ? entity.getOrganization().getAppName() : null)
                .name(entity.getLocationName()).description(entity.getDescription())
                .address(entity.getLocationAddress()).userCount(userCount).build();
    }
}
