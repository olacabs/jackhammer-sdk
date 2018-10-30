package com.olacabs.jch.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HealthCheckResult {
    private String firstSuccess;
    private String lastFailureCause;
    private String lastSuccess;
    private String instanceId;
    private String consecutiveFailures;
    private String lastFailure;
    private String alive;
}
