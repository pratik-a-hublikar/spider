package com.spider.common.response.identity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@Schema(description = "Location response")
public class LocationDTO {
    @Schema(example = "1")
    private Long id;
    private String uuid;
    @Schema(example = "1")
    private Long organizationId;
    @Schema(example = "Head Office")
    private String name;
    private String description;
    private String address;
    private String organizationName;

    @Schema(description = "Number of active users assigned to the location", example = "3")
    private Long userCount;

}
