package com.spider.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "Standard API response envelope")
public class CommonResponse<T> {

    @Schema(description = "Response correlation identifier")
    private String uuid;
    @Schema(description = "Creation timestamp")
    private Date createdAt;
    @Schema(description = "Last update timestamp")
    private Date updatedAt;
    @Schema(description = "User that created the resource")
    private String createdBy;
    @Schema(description = "User that last updated the resource")
    private String updatedBy;
    @Schema(description = "Whether the resource is active")
    private Boolean isActive;
    @Schema(description = "Response payload")
    private T data;

    @Schema(example = "Success")
    private String message;
    public  static <T>  CommonResponse<T> of(String resolve) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setMessage(resolve);
        return response;
    }
    public static <T> CommonResponse<T> of(T data, String resolve) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setMessage(resolve);
        response.setData(data);
        return response;
    }
    public static <T> CommonResponse<T> of(T data) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setMessage("Success");
        response.setData(data);
        return response;
    }
}
