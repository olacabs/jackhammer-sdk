package com.olacabs.jch.sdk.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


@Setter
@Getter
public class JchSDKConfiguration extends Configuration {

    @JsonProperty("websocketClientConfiguration")
    private WebsocketClientConfiguration websocketClientConfiguration;
}
