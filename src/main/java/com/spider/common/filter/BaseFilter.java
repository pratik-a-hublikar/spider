package com.spider.common.filter;

import com.spider.common.AppConstants;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public abstract class BaseFilter extends OncePerRequestFilter {


    public static void unAuthorizedAccess(HttpServletResponse httpServletResponse,
                                          String message,
                                          Integer statusCode) throws IOException {
        JSONObject json = new JSONObject();
        json.put("message", message);
        json.put("status", statusCode);
        httpServletResponse.setHeader("Content-Type", AppConstants.API_APPLICATION_JSON_MEDIA_TYPE);
        httpServletResponse.getWriter().write(json.toString());
        httpServletResponse.setStatus(statusCode);
        httpServletResponse.setContentType(AppConstants.API_APPLICATION_JSON_MEDIA_TYPE);
    }
}
