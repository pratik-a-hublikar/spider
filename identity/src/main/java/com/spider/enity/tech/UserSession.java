package com.spider.enity.tech;


import com.spider.common.model.CommonEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "m_user_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSession extends CommonEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email")
    private String email;

    @Column(name = "session_timeout")
    private Integer timeout = -1;

    @Column(name = "stay_logged_in")
    private boolean stayLoggedIn;

    @Column(name = "login_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginDate;

    @Column(name = "logout_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logOutDate;

    @Column(name = "last_access_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAccessDate;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;



    @Column(name = "token")
    private String token;


}
