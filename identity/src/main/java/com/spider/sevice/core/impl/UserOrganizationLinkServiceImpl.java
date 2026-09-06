package com.spider.sevice.core.impl;


import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.core.UserOrganizationLink;
import com.spider.repository.core.OrganizationRepository;
import com.spider.repository.core.UserRepository;
import com.spider.repository.core.UserOrganizationLinkRepository;
import com.spider.sevice.core.UserOrganizationLinkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Log4j2
@Service
public class UserOrganizationLinkServiceImpl extends CommonServiceImpl<UserOrganizationLink,Long> implements UserOrganizationLinkService {

    private final UserOrganizationLinkRepository userOrganizationLinkRepository;
    private final CriteriaUtil<UserOrganizationLink> criteriaUtil;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    @Autowired
    public UserOrganizationLinkServiceImpl(UserOrganizationLinkRepository userOrganizationLinkRepository,
                                           CriteriaUtil<UserOrganizationLink> criteriaUtil,
                                           UserRepository userRepository,
                                           OrganizationRepository organizationRepository) {
        this.userOrganizationLinkRepository = userOrganizationLinkRepository;
        this.criteriaUtil = criteriaUtil;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }


    @Override
    protected ParentRepository<UserOrganizationLink, Long> getRepository() {
        return userOrganizationLinkRepository;
    }

    @Override
    protected CriteriaUtil<UserOrganizationLink> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    @Transactional
    public void createIfAbsent(Long userId, Long orgId) {
        if (orgId == null) {
            return;
        }
        validateUserAndOrganization(userId, orgId);
        UserOrganizationLink link = userOrganizationLinkRepository
                .findByUserIdAndOrgId(userId, orgId)
                .orElseGet(() -> {
                    UserOrganizationLink newLink = new UserOrganizationLink();
                    newLink.setUserId(userId);
                    newLink.setOrgId(orgId);
                    return newLink;
                });
        link.setIsActive(true);
        link.setIsDeleted(false);
        userOrganizationLinkRepository.save(link);
    }

    @Override
    @Transactional
    public void replaceLinks(Long userId, List<Long> organizationIds) {
        List<Long> requestedIds = organizationIds == null ? List.of() : organizationIds;
        userOrganizationLinkRepository.findByUserId(userId).forEach(link -> {
            if (!requestedIds.contains(link.getOrgId())) {
                link.setIsActive(false);
                link.setIsDeleted(true);
                userOrganizationLinkRepository.save(link);
            }
        });
        requestedIds.forEach(orgId -> createIfAbsent(userId, orgId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserOrganizationLink> getAllLinks(RecordFilter filter) {
        return filter(filter);
    }

    @Override
    @Transactional
    public UserOrganizationLink createLink(UserOrganizationLink link) {
        validateUserAndOrganization(link.getUserId(), link.getOrgId());
        if (userOrganizationLinkRepository.existsByUserIdAndOrgIdAndIsActiveAndIsDeleted(
                link.getUserId(), link.getOrgId(), true, false)) {
            throw new ValidationException("user.organization.already.linked");
        }

        UserOrganizationLink existing = userOrganizationLinkRepository
                .findByUserIdAndOrgId(link.getUserId(), link.getOrgId())
                .orElse(link);
        existing.setUserId(link.getUserId());
        existing.setOrgId(link.getOrgId());
        existing.setIsActive(true);
        existing.setIsDeleted(false);
        return userOrganizationLinkRepository.save(existing);
    }

    @Override
    @Transactional
    public UserOrganizationLink updateLink(Long id, UserOrganizationLink link) {
        UserOrganizationLink current = userOrganizationLinkRepository.findOneActiveById(id);
        if (current == null) {
            throw new ValidationException("user.organization.link.not.found");
        }
        validateUserAndOrganization(link.getUserId(), link.getOrgId());
        if ((!current.getUserId().equals(link.getUserId())
                || !current.getOrgId().equals(link.getOrgId()))
                && userOrganizationLinkRepository.existsByUserIdAndOrgIdAndIsActiveAndIsDeleted(
                link.getUserId(), link.getOrgId(), true, false)) {
            throw new ValidationException("user.organization.already.linked");
        }
        current.setUserId(link.getUserId());
        current.setOrgId(link.getOrgId());
        return userOrganizationLinkRepository.save(current);
    }

    @Override
    @Transactional
    public void deleteLink(Long id) {
        UserOrganizationLink link = userOrganizationLinkRepository.findOneActiveById(id);
        if (link == null) {
            throw new ValidationException("user.organization.link.not.found");
        }
        link.setIsActive(false);
        link.setIsDeleted(true);
        userOrganizationLinkRepository.save(link);
    }

    @Override
    @Transactional
    public void deactivateByUserId(Long userId) {
        userOrganizationLinkRepository.findAll().stream()
                .filter(link -> userId.equals(link.getUserId()))
                .forEach(link -> {
                    link.setIsActive(false);
                    link.setIsDeleted(true);
                    userOrganizationLinkRepository.save(link);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveUserIdsByOrganizationId(Long orgId) {
        return userOrganizationLinkRepository.findActiveUserIdsByOrganizationId(orgId)
                .stream()
                .map(String::valueOf)
                .toList();
    }

    private void validateUserAndOrganization(Long userId, Long orgId) {
        if (userRepository.findOneActiveById(userId) == null) {
            throw new ValidationException("user.not.found");
        }
        if (organizationRepository.findOneActiveById(orgId) == null) {
            throw new ValidationException("organization.not.found");
        }
    }
}
