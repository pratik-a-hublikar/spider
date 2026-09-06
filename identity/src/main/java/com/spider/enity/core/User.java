package com.spider.enity.core;


import com.spider.common.model.CommonEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "m_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends CommonEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "fname", nullable = false)
    private String fname;

    @Column(name = "lname")
    private String lname;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "user_name", nullable = false)
    private String username;

    @Column(name = "is_mail_verified")
    private boolean emailVerified;

    @Column(name = "is_otp_verified")
    private boolean otpVerified;

    @Column(name = "status")
    private Integer statusId;


    @Column(name = "is_super_admin")
    private boolean superAdmin;


}
