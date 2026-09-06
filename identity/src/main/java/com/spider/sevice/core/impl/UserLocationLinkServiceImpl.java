package com.spider.sevice.core.impl;


import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.core.UserLocationLink;
import com.spider.enity.core.Location;
import com.spider.repository.core.LocationRepository;
import com.spider.repository.core.UserLocationLinkRepository;
import com.spider.repository.core.UserRepository;
import com.spider.sevice.core.UserLocationLinkService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
public class UserLocationLinkServiceImpl extends CommonServiceImpl<UserLocationLink,Long> implements UserLocationLinkService {

    private final UserLocationLinkRepository userLocationLinkRepository;
    private final CriteriaUtil<UserLocationLink> criteriaUtil;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    @Autowired
    public UserLocationLinkServiceImpl(UserLocationLinkRepository userLocationLinkRepository,
                                       CriteriaUtil<UserLocationLink> criteriaUtil,
                                       LocationRepository locationRepository,
                                       UserRepository userRepository) {
        this.userLocationLinkRepository = userLocationLinkRepository;
        this.criteriaUtil = criteriaUtil;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }


    @Override
    protected ParentRepository<UserLocationLink, Long> getRepository() {
        return userLocationLinkRepository;
    }

    @Override
    protected CriteriaUtil<UserLocationLink> getCriteriaUtil() {
        return criteriaUtil;
    }

    @Override
    @Transactional
    public void addLink(Long userId, Long locationId) {
        if (userId == null || userRepository.findOneActiveById(userId) == null) {
            throw new ValidationException("user.not.found");
        }
        Location location = locationRepository.findOneActiveById(locationId);
        if (location == null) {
            throw new ValidationException("location.not.found", locationId);
        }

        UserLocationLink link = userLocationLinkRepository.findByUserId(userId).stream()
                .filter(existing -> locationId.equals(existing.getLocationId()))
                .findFirst()
                .orElseGet(UserLocationLink::new);
        link.setUserId(userId);
        link.setLocationId(locationId);
        link.setIsActive(true);
        link.setIsDeleted(false);
        userLocationLinkRepository.save(link);
    }

    @Override
    @Transactional
    public void replaceLinks(Long userId, List<Long> locationIds) {
        List<Long> requestedIds = locationIds == null ? List.of() : locationIds;
        userLocationLinkRepository.findByUserId(userId).forEach(link -> {
            if (!requestedIds.contains(link.getLocationId())) {
                link.setIsActive(false);
                link.setIsDeleted(true);
                userLocationLinkRepository.save(link);
            }
        });

        requestedIds.forEach(locationId -> addLink(userId, locationId));
    }
}
