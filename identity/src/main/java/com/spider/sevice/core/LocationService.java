package com.spider.sevice.core;

import com.spider.common.service.CommonService;
import com.spider.common.request.identity.LocationRequest;
import com.spider.enity.core.Location;
import io.vavr.Tuple2;

import java.util.List;
import java.util.Map;
import com.spider.common.response.identity.LocationUserDTO;

public interface LocationService extends CommonService<Location,Long> {

    Location apply(Location entity, LocationRequest request);

    Location getByUuid(String uuid);

    Location createLocation(LocationRequest request,Long userId);

    Tuple2<Location, Boolean> updateLocation(String uuid, LocationRequest request);

    void softDelete(String uuid);

    List<Location> findByLocationIds(List<Long> ids);

    Map<Long, Long> countActiveUsersByLocationIds(List<Long> locationIds);

    List<LocationUserDTO> getLocationUsers(String locationUuid, Long requesterId);

    void removeUserLocation(String locationUuid, String userUuid, Long requesterId);
}
