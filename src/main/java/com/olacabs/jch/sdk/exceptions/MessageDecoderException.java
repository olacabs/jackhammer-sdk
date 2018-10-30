package com.olacabs.jch.sdk.exceptions;


import com.olacabs.jch.sdk.common.CustomErrorCodes;

public class MessageDecoderException extends AbstractException {

    public MessageDecoderException(String message, Throwable t, CustomErrorCodes code) {
        super(message, t, code);
    }
}
