package com.spider.sevice.core.impl;

import com.spider.enity.core.UserReportingUserLink;
import com.spider.repository.core.UserReportingUserLinkRepository;
import com.spider.sevice.core.UserReportingUserLinkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class UserReportingUserLinkServiceImpl implements UserReportingUserLinkService {

    private final UserReportingUserLinkRepository repository;

    public UserReportingUserLinkServiceImpl(UserReportingUserLinkRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getReportsToUserIds(Long userId) {
        return new LinkedHashSet<>(repository.findActiveReportsToUserIds(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getDirectReportUserIds(Collection<Long> managerIds) {
        if (managerIds == null || managerIds.isEmpty()) {
            return Set.of();
        }
        return new LinkedHashSet<>(repository.findActiveDirectReportIds(managerIds));
    }

    @Override
    @Transactional
    public void replaceLinks(Long userId, Set<Long> reportsToUserIds) {
        Set<Long> requestedIds = reportsToUserIds == null ? Set.of() : reportsToUserIds;

        repository.findByUserId(userId).forEach(link -> {
            if (!requestedIds.contains(link.getReportsToUserId())) {
                link.setIsActive(false);
                link.setIsDeleted(true);
                repository.save(link);
            }
        });

        requestedIds.forEach(managerId -> {
            UserReportingUserLink link = repository
                    .findByUserIdAndReportsToUserId(userId, managerId)
                    .orElseGet(UserReportingUserLink::new);
            link.setUserId(userId);
            link.setReportsToUserId(managerId);
            link.setIsActive(true);
            link.setIsDeleted(false);
            repository.save(link);
        });
    }

    @Override
    @Transactional
    public void deactivateLinksForUser(Long userId) {
        repository.findByUserIdOrReportsToUserId(userId, userId).forEach(link -> {
            link.setIsActive(false);
            link.setIsDeleted(true);
            repository.save(link);
        });
    }
}
