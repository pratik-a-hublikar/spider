package com.spider.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.common.enums.Operator;
import com.spider.common.exception.FilterException;
import com.spider.common.model.ParentEntity;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.filter.FilterCriteria;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.filter.RecordSort;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.CorePage;
import com.spider.common.service.ParentService;
import com.spider.common.utils.CriteriaUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public abstract class ParentServiceImpl <D extends ParentEntity, ID> implements ParentService<D,ID> {
    protected abstract ParentRepository<D,ID> getRepository();
    protected abstract CriteriaUtil<D> getCriteriaUtil();

    @Autowired
    protected ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public <T extends CommonResponse> CorePage<T> filter(RecordFilter recordFilter, Class<T> outputClass) {
        addDataSecurityCheck(recordFilter);
        Page<D> ds = filterAll(recordFilter);
        List<T> response = objectMapper.convertValue(ds.getContent(),objectMapper.getTypeFactory().constructCollectionType(List.class, outputClass));
        return CorePage.of(response, ds);
    }
    @Transactional(readOnly = true)
    public Page<D> filter(RecordFilter recordFilter){
        addDataSecurityCheck(recordFilter);
        return filterAll(recordFilter);
    }
    private static void addDataSecurityCheck(RecordFilter recordFilter) {
        if(CollectionUtils.isEmpty(recordFilter.getFilterCriteria())){
            recordFilter.setFilterCriteria(new ArrayList<>());
        }
        if(recordFilter.getFilterCriteria().stream().anyMatch(p->p.getColumn().equalsIgnoreCase("isDeleted"))){
            recordFilter.getFilterCriteria().stream().filter(p->p.getColumn().equalsIgnoreCase("isDeleted")).forEach(filterCriteria -> filterCriteria.setValues(List.of("false")));
        }else{
            recordFilter.getFilterCriteria().add(new FilterCriteria("isDeleted", Operator.FALSE));
        }
        if(recordFilter.getFilterCriteria().stream().anyMatch(p->p.getColumn().equalsIgnoreCase("isActive"))){
            recordFilter.getFilterCriteria().stream().filter(p->p.getColumn().equalsIgnoreCase("isActive")).forEach(filterCriteria -> filterCriteria.setValues(List.of("true")));
        }else{
            recordFilter.getFilterCriteria().add(new FilterCriteria("isActive", Operator.TRUE));
        }
    }

    @Transactional(readOnly = true)
    public Page<D> filterAll(RecordFilter recordFilter){
        try {
            if(recordFilter.isAll()){
                recordFilter.setPage(0);
                recordFilter.setPageSize(Integer.MAX_VALUE);
            }
            Page<D> entities ;
            PageRequest pageRequest;
            if(recordFilter.getPageSize() == 0){
                recordFilter.setPageSize(1);
            }
            if(recordFilter.getSort() != null){
                RecordSort sort = recordFilter.getSort();
                Sort sortBy = Sort.by(sort.getSortType(), sort.getColumn());
                pageRequest = PageRequest.of(recordFilter.getPage(), recordFilter.getPageSize(), sortBy);
            }else{
                pageRequest = PageRequest.of(recordFilter.getPage(), recordFilter.getPageSize());
            }

            if(!CollectionUtils.isEmpty(recordFilter.getFilterCriteria()) ||
                    !CollectionUtils.isEmpty(recordFilter.getOrCriteria())){
                entities=getRepository().findAll(getCriteriaUtil().toPredicate(recordFilter.getFilterCriteria(),recordFilter.getOrCriteria()),pageRequest);
            }else{
                entities = getRepository().findAll(pageRequest);
            }
            return entities;
        }catch (Exception e){
            log.error("Something went wrong while filtering: ",e);
            throw new FilterException("invalid input,field type,operator or/and value not support");
        }
    }



}
