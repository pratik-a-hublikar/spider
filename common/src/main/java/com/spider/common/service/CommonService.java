package com.spider.common.service;

import com.spider.auth.request.CommonRequest;
import com.spider.common.model.ParentEntity;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.response.CommonPayLoad;
import com.spider.common.response.CommonResponse;
import com.spider.common.response.CorePage;
import org.springframework.data.domain.Page;

public interface ParentService<D extends ParentEntity,ID>{

    <T extends CommonResponse> CorePage<T> filter(RecordFilter recordFilter, Class<T> clazz);
    Page<D> filter(RecordFilter recordFilter);

    default CommonPayLoad<CommonResponse> create(CommonRequest commonRequest,String userId,Long orgId){
        return null;
    }


    default CommonPayLoad<CommonResponse> get(String uuid,Long orgId){
        return null;
    }

    default CommonPayLoad<CommonResponse> update(String uuid,CommonRequest commonRequest,String userId,Long orgId){
        return null;
    }

    default CommonPayLoad<CommonResponse> softDelete(String uuid,String userId,Long orgId){
        return null;
    }
}
