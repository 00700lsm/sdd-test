package com.poc.usermanagement.web;

public final class FieldErrorCodes {

    public static final String ID_REQUIRED = "id.required";
    public static final String ID_LENGTH = "id.length";
    public static final String ID_PATTERN = "id.pattern";
    public static final String ID_DUPLICATE = "id.duplicate";
    public static final String PASSWORD_REQUIRED = "password.required";
    public static final String PASSWORD_LENGTH = "password.length";
    public static final String PASSWORD_SAME_AS_ID = "password.sameAsId";
    public static final String PASSWORD_CURRENT_MISMATCH = "password.currentMismatch";
    public static final String PASSWORD_UNCHANGED = "password.unchanged";
    public static final String NAME_REQUIRED = "name.required";
    public static final String NAME_LENGTH = "name.length";
    public static final String EMAIL_REQUIRED = "email.required";
    public static final String EMAIL_FORMAT = "email.format";
    public static final String EMAIL_LENGTH = "email.length";
    public static final String EMAIL_DUPLICATE = "email.duplicate";
    public static final String LOGIN_FAILED = "login.failed";
    public static final String AUTH_REQUIRED = "auth.required";
    public static final String AUTH_FORBIDDEN = "auth.forbidden";
    public static final String SESSION_KEPT = "session.kept";
    public static final String PAGE_INVALID = "page.invalid";
    public static final String WITHDRAWN_INVALID = "withdrawn.invalid";
    public static final String STATUS_INVALID = "status.invalid";
    public static final String ROLE_INVALID = "role.invalid";
    public static final String ACCOUNT_LOCKED = "account.locked";
    public static final String ADMIN_LAST = "admin.last";

    private FieldErrorCodes() {
    }
}
