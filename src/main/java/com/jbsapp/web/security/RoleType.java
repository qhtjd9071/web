package com.jbsapp.web.security;

public enum RoleType {

    ANONYMOUS("ROLE_ANONYMOUS"),
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN");

    private final String value;

    RoleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
