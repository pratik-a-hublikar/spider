package com.spider.sevice.core;

import com.spider.common.service.CommonService;
import com.spider.common.request.identity.OrganizationRequest;
import com.spider.enity.core.Organization;
import io.vavr.Tuple2;

public interface OrganizationService extends CommonService<Organization,Long> {
    Organization apply(Organization entity, OrganizationRequest request);

    Organization getByUuid(String uuid);

    void softDelete(String uuid);

    Organization createOrganization(OrganizationRequest request);

    Tuple2<Organization,Boolean> updateOrganization(String uuid, OrganizationRequest request);
}
