package com.spider.sevice.core.impl;


import com.spider.common.dto.UserSessionDTO;
import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.common.request.identity.OrganizationRequest;
import com.spider.enity.core.Organization;
import com.spider.repository.core.OrganizationRepository;
import com.spider.repository.core.UserOrganizationLinkRepository;
import com.spider.repository.core.UserRepository;
import com.spider.sevice.core.OrganizationService;
import io.vavr.Tuple2;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Log4j2
@Service
public class OrganizationServiceImpl extends CommonServiceImpl<Organization,Long> implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final CriteriaUtil<Organization> criteriaUtil;
    private final UserOrganizationLinkRepository userOrganizationLinkRepository;
    private final UserRepository userRepository;
    @Autowired
    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   CriteriaUtil<Organization> criteriaUtil,
                                   UserOrganizationLinkRepository userOrganizationLinkRepository,
                                   UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.criteriaUtil = criteriaUtil;
        this.userOrganizationLinkRepository = userOrganizationLinkRepository;
        this.userRepository = userRepository;
    }


    @Override
    protected ParentRepository<Organization, Long> getRepository() {
        return organizationRepository;
    }

    @Override
    protected CriteriaUtil<Organization> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    public Organization apply(Organization entity, OrganizationRequest request) {
        entity.setAppName(request.getName());
        entity.setSuper(false);
        entity.setParentOrg(request.getParentOrganizationId());
        return entity;
    }

    @Override
    public Organization getByUuid(String uuid) {
        Organization organization = organizationRepository.findOneActiveByUUID(uuid);
        if (organization == null) {
            throw new ValidationException("organization.not.found");
        }
        return organization;
    }

    @Override
    protected void addEntityDataSecurityCheck(RecordFilter recordFilter, UserSessionDTO userSession) {
        if (userSession == null || userSession.getUserId() == null
                || userRepository.findOneActiveById(userSession.getUserId()) == null) {
            appendAllowedOrganizationIds(recordFilter, Collections.emptySet());
            return;
        }

        Set<Long> accessibleOrganizationIds = new LinkedHashSet<>(
                userOrganizationLinkRepository.findActiveOrganizationIdsByUserId(userSession.getUserId()));
        Set<Long> organizationsToExpand = new LinkedHashSet<>(accessibleOrganizationIds);

        while (!organizationsToExpand.isEmpty()) {
            Set<Long> childOrganizationIds = new LinkedHashSet<>(
                    organizationRepository.findActiveChildOrganizationIds(organizationsToExpand));
            childOrganizationIds.removeAll(accessibleOrganizationIds);
            if (childOrganizationIds.isEmpty()) {
                break;
            }
            accessibleOrganizationIds.addAll(childOrganizationIds);
            organizationsToExpand = childOrganizationIds;
        }

        appendAllowedOrganizationIds(recordFilter, accessibleOrganizationIds);
    }

    private void appendAllowedOrganizationIds(RecordFilter recordFilter, Set<Long> organizationIds) {
        List<String> values = organizationIds.isEmpty()
                ? List.of("-1")
                : organizationIds.stream().sorted().map(String::valueOf).toList();
        recordFilter.appendCriteria("id", "in", values);
    }

    @Override
    public void softDelete(String uuid) {
        Organization entity = getByUuid(uuid);
        Long id = entity.getId();
        if(entity.isSuper()){
            throw new ValidationException("organization.delete.super");
        }
        long activeChildCount = organizationRepository.countActiveChildOrganizations(id);
        if (activeChildCount > 0 ) {
            log.warn("Cannot change parent organization for organization with id: {} as it has active child organizations.", id);
            throw new ValidationException("organization.delete.has.children");
        }

        entity.setIsActive(false);
        entity.setIsDeleted(true);
        create(entity);
    }

    @Override
    public Organization createOrganization(OrganizationRequest request) {
        Organization entity = new Organization();
        apply(entity, request);
        if(organizationRepository.findOneByAppNameAndIsActiveAndIsDeleted(request.getName(), true, false) != null){
            throw new ValidationException("organization.name.exists", request.getName());
        }
        create(entity);
        return entity;
    }

    @Override
    public Tuple2<Organization,Boolean> updateOrganization(String uuid, OrganizationRequest request) {

        try {
            Organization entity = getByUuid(uuid);
            Long id = entity.getId();
            apply(entity, request);
            if(organizationRepository.findOneByAppNameAndIdNotAndIsActiveAndIsDeleted(request.getName(), id, true, false) != null){
                throw new ValidationException("organization.name.exists", request.getName());
            }
            create(entity);
            return new Tuple2<>(entity, true);
        } catch (ValidationException e){
            throw e;
        }catch (Exception e){
            log.error("Error occurred while updating organization with uuid: {}", uuid, e);
            return new Tuple2<>(null, false);
        }
    }
}
