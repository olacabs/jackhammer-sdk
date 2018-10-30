package com.olacabs.jch.sdk.exceptions.handlers;

import javax.ws.rs.core.Response;

import com.olacabs.jch.sdk.common.ExceptionMessages;
import com.olacabs.jch.sdk.models.ErrorResponseModel;
import com.olacabs.jch.sdk.common.CustomErrorCodes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionHandler {

    public Response handle(Throwable exception)  {
        ErrorResponseModel model = new ErrorResponseModel();
        model.setErrorCode(CustomErrorCodes.SERVICE_INTERNAL_EXCEPTION);
        model.setMessage(ExceptionMessages.INTERNAL_ERROR);
        log.error("Unknown error...", exception);
        return Response.status(CustomErrorCodes.INTERNAL_ERROR.getValue()).entity(model).build();
    }
}
