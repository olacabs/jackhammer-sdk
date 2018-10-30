package com.olacabs.jch.sdk.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class WebsocketClientConfiguration {


    @NotNull
    @JsonProperty
    private String serverHost;

    @NotNull
    @JsonProperty
    private Integer threadPoolSize;

    @NotNull
    @JsonProperty
    private Integer initialDelay;

    @NotNull
    @JsonProperty
    private Integer period;

    @JsonProperty
    private String localServer;

    public String getServerHost() {
        return serverHost;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public Integer getInitialDelay() {
        return initialDelay;
    }

    public Integer getPeriod() {
        return period;
    }

    public String getLocalServer() { return localServer;}
}
