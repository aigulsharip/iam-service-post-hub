package com.post_hub.iam_service.model.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiLogMessage {
    POST_INFO_BY_ID("Receiving post with ID: {}"),
    NAME_OF_CURRENT_METHOD("Current method: {}"),
    ;

    private final String value;
}
