package com.spider.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Operator {

    CONTAINS("like"),
    EQUAL("="),
    NOT_EQUAL("!="),
    IN("in"),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN_EQUAL("<="),
    TRUE("is_true"),
    FALSE("is_false"),
    NULL("is_null"),
    NOT_NULL("is_not_null"),
    CONTAINS_OR_EQUAL("likeOrEqual"),
    BETWEEN("between");

    private static final Map<String, Operator> map = new HashMap<>();


    static {
        for (Operator legEnum : Operator.values()) {
            map.put(legEnum.operatorValue, legEnum);
        }
    }
    private final String operatorValue;
    Operator(String operatorValue) {
        this.operatorValue = operatorValue;
    }

    public static Operator getOperator(String operatorValue) {
        return map.get(operatorValue);
    }

}
