package com.post_hub.iam_service.model.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiErrorMessage {
    POST_NOT_FOUND_BY_ID("Post with ID: %s was not found"),
    POST_ALREADY_EXISTS("Post with title: %s already exists"),
    USER_NOT_FOUND_BY_ID("User with ID: %s was not found"),
    USER_ALREADY_USERNAME_EXISTS("User with this username or email: %s already exists"),
    USER_ALREADY_EMAIL_EXISTS("User with this email: %s already exists"),
    ROLE_NOT_FOUND_BY_NAME("Role with name was not found"),
    EMAIL_NOT_FOUND("Email was not found"),
    INVALID_TOKEN_SIGNATURE("Invalid token signature"),
    ERROR_DURING_JWT_PROCESSING("An unexpected error occurred during JWT processing"),
    TOKEN_EXPIRED("Token expired."),
    UNEXPECTED_ERROR_OCCURRED("An unexpected error occurred. Please try again later."),

    AUTHENTICATION_FAILED_FOR_USER("Authentication failed for user: {}. "),
    INVALID_USER_OR_PASSWORD("Invalid email or password. Try again"),
    INVALID_USER_REGISTRATION_STATUS("Invalid user registration status: %s. "),
    NOT_FOUND_REFRESH_TOKEN("Refresh token not found."),

    MISMATCH_PASSWORDS("Password does not match"),
    INVALID_PASSWORD("Invalid password. It must have: "
            + "length at least " + ApiConstants.REQUIRED_MIN_PASSWORD_LENGTH + ", including "
            + ApiConstants.REQUIRED_MIN_LETTERS_NUMBER_EVERY_CASE_IN_PASSWORD + " letter(s) in upper and lower cases, "
            + ApiConstants.REQUIRED_MIN_CHARACTERS_NUMBER_IN_PASSWORD + " character(s), "
            + ApiConstants.REQUIRED_MIN_DIGITS_NUMBER_IN_PASSWORD + " digit(s). "),


    ;

    private final String message;

    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}