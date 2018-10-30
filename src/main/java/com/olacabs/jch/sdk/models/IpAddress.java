package com.olacabs.jch.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IpAddress {
    private String protocol;
    private String ipAddress;
}
