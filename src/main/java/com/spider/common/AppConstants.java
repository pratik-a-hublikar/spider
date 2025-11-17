package com.spider.common;

public class AppConstants {


    public static final String API_APPLICATION_JSON_MEDIA_TYPE = "application/json";
    public static final String VALIDATION_FILTER_COLUMN = "Filter column should not be null";
    public static final String VALIDATION_FILTER_OPERATOR = "Filter operator should not be null";
    public static final String PAGE_COUNT_CONSTRAINT = "Page  must be at least 1";
    public static final String PAGE_SIZE_CONSTRAINT = "Page size must be at least 1";

    public static final String USERNAME_IS_MANDATORY = "Username is mandatory";

    public static final String PASSWORD_IS_MANDATORY = "Password is mandatory";

    public static final String LOGIN_FAILED = "Email or Password is wrong, please try to login again!";
    public static final String LOGIN_SUCCESS = "Login Success!";



    //API paths
    public static final String AUTH_ENDPOINT = "/public/auth";
    public static final String USER_SIGN_IN = "/signin";
    public static final String LOGIN = "LOGIN";

    public static final String AUTHORIZATION = "Authorization";
    public static final String MASTER = "/master";
    public static final String API_MASTER = MASTER+"/api";
    public static final String API_MODULE_MASTER = MASTER+"/api-module";

    public static final String MODULE_MASTER = MASTER+"/module";
    public static final String DEPT_MODULE_MASTER = MASTER+"/dept-module";

    public static final String DEPT_MASTER = MASTER+"/dept";
    public static final String USER_MASTER = MASTER+"/user";
    public static final String USER_DEPT_MASTER = MASTER+"/user-dept";




}
