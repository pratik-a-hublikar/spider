package com.spider.common.request.filter;


import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class RecordFilter extends FilterRequest implements Serializable {



    public void appendCriteria(String filedName, String operator, List<String> values){
        if(Objects.isNull(getFilterCriteria())){
            setFilterCriteria(new ArrayList<>());
        }
        getFilterCriteria().add(new FilterCriteria(filedName,operator, values));
    }

    public List<FilterCriteria> filterCriteria(List<FilterCriteria> criteria, List<String> fieldName){
        fieldName = fieldName.stream().map(String::toLowerCase).toList();
        if(CollectionUtils.isEmpty(criteria)){
            return null;
        }
        List<String> finalFieldName = fieldName;
        return criteria.stream().filter(cr -> finalFieldName.contains(cr.getColumn().toLowerCase())).toList();
    }


}
