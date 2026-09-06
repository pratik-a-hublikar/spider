package com.spider.sevice.core;

import java.util.Collection;
import java.util.Set;

/**
 * Maintains the user reporting hierarchy.
 * A link reads as: userId reports to reportsToUserId.
 */
public interface UserReportingUserLinkService {

    Set<Long> getReportsToUserIds(Long userId);

    Set<Long> getDirectReportUserIds(Collection<Long> managerIds);

    void replaceLinks(Long userId, Set<Long> reportsToUserIds);

    void deactivateLinksForUser(Long userId);
}
