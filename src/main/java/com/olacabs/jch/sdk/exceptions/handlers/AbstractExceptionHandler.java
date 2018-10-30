package com.olacabs.jch.sdk.exceptions.handlers;

import com.olacabs.jch.sdk.common.CustomErrorCodes;
import com.olacabs.jch.sdk.exceptions.AbstractException;
import com.olacabs.jch.sdk.models.ErrorResponseModel;

import lombok.extern.slf4j.Slf4j;
import javax.ws.rs.core.Response;

@Slf4j
public class AbstractExceptionHandler {

    public Response handle(AbstractException exception)  {
        ErrorResponseModel model = new ErrorResponseModel();
        model.setErrorCode(exception.getCode());
        model.setMessage(exception.getCode().name());
        log.error("Unknown error...", exception);
        return Response.status(CustomErrorCodes.INTERNAL_ERROR.getValue()).entity(model).build();
    }
}
