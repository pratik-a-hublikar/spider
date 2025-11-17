package com.spider.common.request.filter;

import com.spider.common.AppConstants;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class FilterRequest {
    @Min(value = 0, message = AppConstants.PAGE_COUNT_CONSTRAINT)
    private int page;
    @Min(value = 0, message = AppConstants.PAGE_SIZE_CONSTRAINT)
    private int pageSize;
    private boolean all;
    private RecordSort sort;
    private List<FilterCriteria> filterCriteria;
    private List<FilterCriteria> orCriteria;

}
