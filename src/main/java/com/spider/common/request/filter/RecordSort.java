package com.spider.common.request.filter;

import lombok.Data;

@Data
public class RecordSort {
    private String column = "id";
    private String sortType = "ASC";
}
