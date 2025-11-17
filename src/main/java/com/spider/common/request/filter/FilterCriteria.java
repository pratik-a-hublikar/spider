package com.spider.common.request.filter;

import com.spider.common.AppConstants;
import com.spider.common.enums.Operator;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FilterCriteria implements Serializable {


    @Serial
    private static final long serialVersionUID = -3591151185890623788L;

    @NotBlank(message = AppConstants.VALIDATION_FILTER_COLUMN)
    private String column;

    @NotBlank(message = AppConstants.VALIDATION_FILTER_OPERATOR)
    private String operator;

    private List<String> values;


    public FilterCriteria(String column, String operator, List<String> values) {
        this.column = column;
        this.operator = operator;
        this.values = values;
    }
    public FilterCriteria(String column, Operator operator) {
        this.column = column;
        this.operator = operator.getOperatorValue();
    }
}
