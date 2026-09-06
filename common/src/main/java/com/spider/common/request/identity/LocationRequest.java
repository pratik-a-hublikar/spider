package com.spider.common.request.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Location create or update request")
public class LocationRequest {
    @NotNull
    @Schema(description = "Owning organization identifier", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long organizationId;
    @NotBlank
    @Schema(description = "Location name", example = "Head Office", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 250, message = "validation.name.max")
    private String name;

    @Schema(description = "Location description", example = "Primary corporate office")
    @Size(max = 250, message = "validation.description.max")
    private String description;

    @Size(max = 250, message = "validation.address.max")
    @Schema(description = "Location postal address", example = "123 Main Street")
    private String address;
}
