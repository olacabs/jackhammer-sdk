package com.olacabs.jch.sdk.exceptions;


import com.olacabs.jch.sdk.common.CustomErrorCodes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoMatchingProviderException extends AbstractException {
    public NoMatchingProviderException(String message, Throwable t, CustomErrorCodes code) {
        super(message, t, code);
    }
}
