package com.spider.common.model;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class CommonEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false,updatable = false,unique = true,length = 36)
    private String uuid;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at",columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date updatedAt;

    @Column(name = "created_by", nullable = false, length = 50)
    private Long createdBy;

    @Column(name = "updated_by", length = 50)
    private Long updatedBy;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;


    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        createdAt = now;
        updatedAt = now;
        isActive = true;
        isDeleted = false;
        uuid = UUID.randomUUID().toString();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            Object userId = attributes.getRequest().getAttribute("userId");
            if (userId instanceof Long) {
                createdBy = (Long) userId;
                updatedBy = (Long) userId;
            }else {
                createdBy = 1L;
                updatedBy = 1L;
            }
        }else {
            createdBy = 1L;
            updatedBy = 1L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            Object userId = attributes.getRequest().getAttribute("userId");
            if (userId instanceof Long) {
                updatedBy = (Long) userId;
            }else {
                updatedBy = 1L;
            }
        }else {
            updatedBy = 1L;
        }
    }


}
