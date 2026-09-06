package com.spider.rest.core;

import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.OrganizationRequest;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.identity.OrganizationDTO;
import com.spider.common.rest.BaseResource;
import com.spider.enity.core.Organization;
import com.spider.sevice.core.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vavr.Tuple2;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/master/organization")
@Tag(name = "Organizations")
@SecurityRequirement(name = "bearerAuth")
public class OrganisationController extends BaseResource {
    private final OrganizationService service;

    public OrganisationController(OrganizationService service) {
        this.service = service;
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter organizations")
    public CommonResponse<Page<OrganizationDTO>> filter(@RequestBody RecordFilter filter) {
        return CommonResponse.of(service.filter(filter).map(this::toDto), resolve("common.success"));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get an organization")
    public CommonResponse<OrganizationDTO> get(@PathVariable String uuid) {
        return CommonResponse.of(toDto(service.getByUuid(uuid)), resolve("common.success"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create an organization")
    public CommonResponse<OrganizationDTO> create(@RequestBody @Valid OrganizationRequest request) {
        Organization entity = service.createOrganization(request);
        return CommonResponse.of(toDto(entity), resolve("organization.create.success"));
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Update an organization")
    public CommonResponse<OrganizationDTO> update(@PathVariable String uuid,
                                                   @RequestBody @Valid OrganizationRequest request) {
        Tuple2<Organization, Boolean> objects = service.updateOrganization(uuid, request);

        return CommonResponse.of(toDto(objects._1()), objects._2()
                ? resolve("organization.update.success")
                : resolve("organization.update.fail"));
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Deactivate an organization")
    public CommonResponse<Object> delete(@PathVariable String uuid) {
        service.softDelete(uuid);
        return CommonResponse.of(resolve("organization.delete.success"));
    }

    private OrganizationDTO toDto(Organization entity) {
        if (entity == null) return null;
        return OrganizationDTO.builder().id(entity.getId()).uuid(entity.getUuid()).name(entity.getAppName())
                .superOrganization(entity.isSuper()).parentOrganizationId(entity.getParentOrg())
                .parentOrganizationName(entity.getParentOrganization() != null ? entity.getParentOrganization().getAppName() : null)
                .build();
    }
}
