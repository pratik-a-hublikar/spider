package com.spider.enity.core;

import com.spider.common.model.CommonEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "m_user_reporting_user_link")
@Getter
@Setter
@NoArgsConstructor
public class UserReportingUserLink extends CommonEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reports_to_user_id", nullable = false)
    private Long reportsToUserId;
}
