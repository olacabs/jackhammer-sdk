package com.olacabs.jch.sdk.common;

public enum CustomErrorCodes {

    // http response
    INTERNAL_ERROR(500),

    //custom codes
    SERVICE_INTERNAL_EXCEPTION(1000),
    TEMP_DIR_CREATION_ERROR(1001),
    GIT_CLONE_ERROR(1002),
    TEMP_DIR_SYSTEM_PROPERTY(1003),
    TEMP_ROOT_DIR_CREATION(1004),
    PROVIDER_NOT_FOUND(1005);

    private final int value;
    private CustomErrorCodes(int value)
    {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
