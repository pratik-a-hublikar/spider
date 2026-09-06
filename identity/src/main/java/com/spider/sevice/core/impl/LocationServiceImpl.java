package com.spider.sevice.core.impl;


import com.spider.common.dto.UserSessionDTO;
import com.spider.common.exception.ValidationException;
import com.spider.common.repository.ParentRepository;
import com.spider.common.request.filter.RecordFilter;
import com.spider.common.request.identity.LocationRequest;
import com.spider.common.response.identity.LocationUserDTO;
import com.spider.common.service.impl.CommonServiceImpl;
import com.spider.common.util.CriteriaUtil;
import com.spider.enity.core.Location;
import com.spider.repository.core.LocationRepository;
import com.spider.repository.core.OrganizationRepository;
import com.spider.repository.core.UserOrganizationLinkRepository;
import com.spider.repository.core.UserRepository;
import com.spider.repository.core.UserLocationLinkRepository;
import com.spider.sevice.core.LocationService;
import com.spider.sevice.core.UserLocationLinkService;
import io.vavr.Tuple2;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;

@Log4j2
@Service
public class LocationServiceImpl extends CommonServiceImpl<Location,Long> implements LocationService {

    private final LocationRepository locationRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationLinkRepository userOrganizationLinkRepository;
    private final UserRepository userRepository;
    private final UserLocationLinkService userLocationLinkService;
    private final UserLocationLinkRepository userLocationLinkRepository;
    private final CriteriaUtil<Location> criteriaUtil;
    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository,
                               OrganizationRepository organizationRepository,
                               UserOrganizationLinkRepository userOrganizationLinkRepository,
                               UserRepository userRepository,
                               UserLocationLinkService userLocationLinkService,
                               UserLocationLinkRepository userLocationLinkRepository,
                               CriteriaUtil<Location> criteriaUtil) {
        this.locationRepository = locationRepository;
        this.organizationRepository = organizationRepository;
        this.userOrganizationLinkRepository = userOrganizationLinkRepository;
        this.userRepository = userRepository;
        this.userLocationLinkService = userLocationLinkService;
        this.userLocationLinkRepository = userLocationLinkRepository;
        this.criteriaUtil = criteriaUtil;
    }


    @Override
    protected ParentRepository<Location, Long> getRepository() {
        return locationRepository;
    }

    @Override
    protected CriteriaUtil<Location> getCriteriaUtil() {
        return criteriaUtil;
    }


    @Override
    public Location apply(Location entity, LocationRequest request) {
        entity.setOrgId(request.getOrganizationId());
        entity.setLocationName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setLocationAddress(request.getAddress());
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public Location getByUuid(String uuid) {
        return requireLocation(uuid);
    }

    @Override
    @Transactional
    public Location createLocation(LocationRequest request, Long userId) {
        Location entity = new Location();
        apply(entity, request);
        if (locationRepository.findOneByLocationNameAndOrgIdAndIsActiveAndIsDeleted(
                request.getName(), request.getOrganizationId(), true, false) != null) {
            throw new ValidationException("location.name.exists", request.getName());
        }

        create(entity);
        userLocationLinkService.addLink(userId, entity.getId());
        return entity;
    }

    @Override
    public Tuple2<Location, Boolean> updateLocation(String uuid, LocationRequest request) {
        try {
            Location entity = requireLocation(uuid);
            Long id = entity.getId();
            apply(entity, request);
            if (locationRepository.findOneByLocationNameAndOrgIdAndIdNotAndIsActiveAndIsDeleted(
                    request.getName(), request.getOrganizationId(), id, true, false) != null) {
                throw new ValidationException("location.name.exists", request.getName());
            }
            create(entity);
            return new Tuple2<>(entity, true);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while updating location with uuid: {}", uuid, e);
            return new Tuple2<>(null, false);
        }
    }

    @Override
    public void softDelete(String uuid) {
        Location entity = requireLocation(uuid);
        long assignedUsers = userLocationLinkRepository
                .countActiveUsersByLocationIds(List.of(entity.getId())).stream()
                .mapToLong(row -> (Long) row[1])
                .sum();
        if (assignedUsers > 0) {
            throw new ValidationException("location.delete.assigned.users", assignedUsers);
        }
        entity.setIsActive(false);
        entity.setIsDeleted(true);
        create(entity);
    }

    @Override
    public List<Location> findByLocationIds(List<Long> ids) {
        return super.findAllByIdInAndActiveAndDeleted(ids,true,false);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countActiveUsersByLocationIds(List<Long> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> counts = new LinkedHashMap<>();
        userLocationLinkRepository.countActiveUsersByLocationIds(locationIds)
                .forEach(row -> counts.put((Long) row[0], (Long) row[1]));
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationUserDTO> getLocationUsers(String locationUuid, Long requesterId) {
        Location location = requireLocation(locationUuid);
        validateLocationAccess(location, requesterId);
        return userRepository.findActiveUsersByLocationId(location.getId()).stream()
                .map(user -> LocationUserDTO.builder()
                        .id(user.getId())
                        .uuid(user.getUuid())
                        .username(user.getUsername())
                        .firstName(user.getFname())
                        .lastName(user.getLname())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void removeUserLocation(String locationUuid, String userUuid, Long requesterId) {
        Location location = requireLocation(locationUuid);
        validateLocationAccess(location, requesterId);
        var user = userRepository.findOneActiveByUUID(userUuid);
        if (user == null) throw new ValidationException("user.not.found");
        var link = userLocationLinkRepository
                .findByUserIdAndLocationIdAndIsActiveAndIsDeleted(user.getId(), location.getId(), true, false)
                .orElseThrow(() -> new ValidationException("user.location.link.not.found"));
        link.setIsActive(false);
        link.setIsDeleted(true);
        userLocationLinkRepository.save(link);
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
        recordFilter.appendCriteria("orgId", "in", values);
    }

    private void validateLocationAccess(Location location, Long requesterId) {
        var requester = requesterId == null ? null : userRepository.findOneActiveById(requesterId);
        if (requester == null) {
            throw new ValidationException("location.access.denied");
        }
        if (requester.isSuperAdmin()) {
            return;
        }

        Set<Long> accessibleOrganizationIds = new LinkedHashSet<>(
                userOrganizationLinkRepository.findActiveOrganizationIdsByUserId(requesterId));
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
        if (!accessibleOrganizationIds.contains(location.getOrgId())) {
            throw new ValidationException("location.access.denied");
        }
    }

    private Location requireLocation(String uuid) {
        Location location = uuid == null ? null : locationRepository.findOneActiveByUUID(uuid);
        if (location == null) {
            throw new ValidationException("location.not.found", uuid);
        }
        return location;
    }
}
