package com.olacabs.jch.sdk.exceptions;

import com.olacabs.jch.sdk.common.CustomErrorCodes;

public class GitCloneException extends AbstractException {

    public GitCloneException(String message, Throwable t, CustomErrorCodes code) {
        super(message, t, code);
    }
}
