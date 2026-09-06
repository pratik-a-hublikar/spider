package com.spider.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.common.dto.UserSessionDTO;
import com.spider.common.enums.Operator;
import com.spider.common.exception.FilterException;
import com.spider.common.model.CommonEntity;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.filter.FilterCriteria;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.filter.RecordSort;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.CorePage;
import com.spider.common.service.CommonService;
import com.spider.common.util.CriteriaUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Log4j2
public abstract class CommonServiceImpl<D extends CommonEntity, ID> implements CommonService<D,ID> {
    protected abstract ParentRepository<D,ID> getRepository();
    protected abstract CriteriaUtil<D> getCriteriaUtil();

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MessageSource messageSource;

    public String resolve(String key, String... arg) {
        return messageSource.getMessage(key, arg, LocaleContextHolder.getLocale());
    }

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
    private void addDataSecurityCheck(RecordFilter recordFilter) {
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

        UserSessionDTO userSession = getCurrentUserSession();
        if(userSession == null || !userSession.isSuperAdmin()){
            addEntityDataSecurityCheck(recordFilter, userSession);
        }
    }

    /**
     * Allows an entity service to add mandatory server-side filters. Client criteria are retained,
     * so the security criteria always narrow (and never widen) the requested result set.
     */
    protected void addEntityDataSecurityCheck(RecordFilter recordFilter, UserSessionDTO userSession) {
        // No entity-specific restriction by default.
    }

    private UserSessionDTO getCurrentUserSession() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        Object session = attributes.getRequest().getAttribute("userSession");
        return session instanceof UserSessionDTO ? (UserSessionDTO) session : null;
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
            throw new FilterException("filter.invalid.input");
        }
    }

    public List<D> findAllByIds(List<ID> ids){
        return getRepository().findAllById(ids);
    }


    public List<D> findAllByIdInAndActiveAndDeleted(List<ID> ids, boolean isActive, boolean isDeleted){
        return getRepository().findAllByIdInAndIsActiveAndIsDeleted(ids,isActive,isDeleted);
    }

    @Override
    public D get(Long id) {
        return getRepository().findOneActiveById(id);
    }

    @Override
    public List<D> get(Collection<Long> id) {
        return getRepository().findAllById((Iterable<ID>) id);
    }

    @Override
    public void create(Collection<D> enities) {
        enities.forEach(entity -> {
            getRepository().save(entity);
        });
    }

    @Override
    public void create(D entity) {
        getRepository().save(entity);
    }
}
