package com.poc.usermanagement.user;

import com.poc.usermanagement.web.FieldErrorCodes;
import com.poc.usermanagement.web.FieldFailure;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AccountRules {

    private AccountRules() {
    }

    public static String normalizeId(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeEmail(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    public static void addIdErrors(List<FieldFailure> errors, String normalizedId) {
        if (normalizedId.isEmpty()) {
            errors.add(new FieldFailure("loginId", FieldErrorCodes.ID_REQUIRED));
            return;
        }
        if (normalizedId.length() < 4 || normalizedId.length() > 20) {
            errors.add(new FieldFailure("loginId", FieldErrorCodes.ID_LENGTH));
        }
        if (!normalizedId.matches("[a-z][a-z0-9_]{3,19}")) {
            errors.add(new FieldFailure("loginId", FieldErrorCodes.ID_PATTERN));
        }
    }

    public static void addNewPasswordErrors(List<FieldFailure> errors, String field, String password, String loginId) {
        if (password == null || password.isEmpty()) {
            errors.add(new FieldFailure(field, FieldErrorCodes.PASSWORD_REQUIRED));
            return;
        }
        if (password.length() < 8 || password.length() > 64) {
            errors.add(new FieldFailure(field, FieldErrorCodes.PASSWORD_LENGTH));
        }
        if (loginId != null && !loginId.isEmpty() && password.equals(loginId)) {
            errors.add(new FieldFailure(field, FieldErrorCodes.PASSWORD_SAME_AS_ID));
        }
    }

    public static void addNameErrors(List<FieldFailure> errors, String name) {
        if (name.isEmpty()) {
            errors.add(new FieldFailure("name", FieldErrorCodes.NAME_REQUIRED));
            return;
        }
        if (name.length() > 50) {
            errors.add(new FieldFailure("name", FieldErrorCodes.NAME_LENGTH));
        }
    }

    public static void addEmailErrors(List<FieldFailure> errors, String email) {
        if (email.isEmpty()) {
            errors.add(new FieldFailure("email", FieldErrorCodes.EMAIL_REQUIRED));
            return;
        }
        if (email.length() > 254) {
            errors.add(new FieldFailure("email", FieldErrorCodes.EMAIL_LENGTH));
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+")) {
            errors.add(new FieldFailure("email", FieldErrorCodes.EMAIL_FORMAT));
        }
    }

    public static String likePattern(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        String escaped = query.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    public static List<FieldFailure> copy(List<FieldFailure> errors) {
        return new ArrayList<>(errors);
    }
}
