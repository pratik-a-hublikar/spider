package com.spider.common.response.identity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LocationUserDTO {
    private Long id;
    private String uuid;
    private String username;
    private String firstName;
    private String lastName;
}
