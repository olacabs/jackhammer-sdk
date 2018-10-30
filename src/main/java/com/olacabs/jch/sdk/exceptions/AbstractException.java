package com.olacabs.jch.sdk.exceptions;

import com.olacabs.jch.sdk.common.CustomErrorCodes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbstractException extends Exception {

    private CustomErrorCodes code;
    public AbstractException(String message, Throwable t,CustomErrorCodes code) {
        super(message,t);
        this.code = code;
    }
}
