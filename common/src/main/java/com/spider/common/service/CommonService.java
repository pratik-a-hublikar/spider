package com.spider.common.service;

import com.spider.common.model.CommonEntity;
import com.spider.common.request.CommonRequest;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.response.CommonPayLoad;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.CorePage;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;

public interface CommonService<D extends CommonEntity,ID>{

    <T extends CommonResponse> CorePage<T> filter(RecordFilter recordFilter, Class<T> clazz);
    Page<D> filter(RecordFilter recordFilter);

    default CommonPayLoad<CommonResponse> create(CommonRequest commonRequest, String userId, Long orgId){
        return null;
    }


    default CommonPayLoad<CommonResponse> get(String uuid,Long orgId){
        return null;
    }

    D get(Long id);
    default CommonPayLoad<CommonResponse> update(String uuid,CommonRequest commonRequest,String userId,Long orgId){
        return null;
    }

    void create(Collection<D> d);
    void create(D d);

    List<D> get(Collection<Long> id);

    default CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId){
        return null;
    }
}
