package com.poc.usermanagement.web;

public final class FieldFailure {

    private final String field;
    private final String code;

    public FieldFailure(String field, String code) {
        this.field = field;
        this.code = code;
    }

    public String getField() {
        return field;
    }

    public String getCode() {
        return code;
    }
}
