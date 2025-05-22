package com.post_hub.iam_service.utils;

import com.post_hub.iam_service.model.constants.ApiConstants;

public class ApiUtils {

    public static String getMethodName() {
        try {
            return new Throwable().getStackTrace()[1].getMethodName();
        } catch (Exception cause) {
            return ApiConstants.UNDEFINED;
        }
    }
}
