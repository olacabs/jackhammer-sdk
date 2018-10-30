package com.olacabs.jch.sdk.exceptions;

import com.olacabs.jch.sdk.common.CustomErrorCodes;

public class TempDirCreationException extends AbstractException {
    public TempDirCreationException(String message, Throwable t, CustomErrorCodes code) {
        super(message, t, code);
    }
}
