package com.post_hub.iam_service.utils;

import com.post_hub.iam_service.model.constants.ApiConstants;
import com.post_hub.iam_service.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApiUtils {

    private final JwtTokenProvider jwtTokenProvider;
    public static String getMethodName() {
        try {
            return new Throwable().getStackTrace()[1].getMethodName();
        } catch (Exception cause) {
            return ApiConstants.UNDEFINED;
        }
    }

    public static Cookie createAuthCookie(String value) {
        Cookie authorizationCookie = new Cookie(HttpHeaders.AUTHORIZATION, value);
        authorizationCookie.setHttpOnly(true);
        authorizationCookie.setSecure(true);
        authorizationCookie.setPath("/");
        authorizationCookie.setMaxAge(300);
        return authorizationCookie;
    }

    public static String generateUuidWithDash() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String getCurrentUsername () {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static String generateUuidWithoutDash() {
        return UUID.randomUUID().toString().replace(ApiConstants.DASH, StringUtils.EMPTY);
    }

    public Integer getUserIdFromAuthentication() {
        String jwtToken = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        return Integer.parseInt(jwtTokenProvider.getUserId(jwtToken));
    }
}
