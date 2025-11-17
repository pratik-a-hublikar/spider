package com.spider.common.enums;

public enum RecordSortType {
    NONE("none", RecordSortOrder.UNSORTED), ASC("asc", RecordSortOrder.ASCENDING), DESC("desc", RecordSortOrder.DESCENDING);

    private final String value;
    private final RecordSortOrder order;

    RecordSortType(String value, RecordSortOrder order) {
        this.value = value;
        this.order = order;
    }
    RecordSortType() {
        this.value = "none";
        this.order = RecordSortOrder.UNSORTED;
    }

}
