package com.olacabs.jch.sdk.application;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter(AccessLevel.PACKAGE)
@Getter
public class InstanceDetails {
    private static InstanceDetails instanceDetails;
    private   String instanceId;


    public static InstanceDetails getInstanceDetails(){
        if(null == instanceDetails )
            instanceDetails = new InstanceDetails();

        return instanceDetails;
    }
}
