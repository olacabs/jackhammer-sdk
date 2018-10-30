package com.olacabs.jch.sdk.models;

import com.olacabs.jch.sdk.common.CustomErrorCodes;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AbstractResponse {

    private String message;
    private int errorCode;

    public void setErrorCode(CustomErrorCodes code) {
        this.errorCode = code.getValue();
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public int getErrorCode(){
        return errorCode;
    }

    public String getMessage(){
        return message;
    }

}
