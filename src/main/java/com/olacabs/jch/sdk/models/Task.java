package com.olacabs.jch.sdk.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Task {
    private String id;
    private List<HealthCheckResult> healthCheckResults;
    private List<Integer> ports;
    private List<Integer> servicePorts;
    private String host;
    private String appId;
    private String stagedAt;
    private String startedAt;
    private String state;
    private List<IpAddress> ipAddresses;
    private String slaveId;
    private String version;
    private List<String> localVolumes;
}
