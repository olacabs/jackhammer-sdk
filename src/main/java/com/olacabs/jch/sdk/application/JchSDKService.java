package com.olacabs.jch.sdk.application;

import com.hubspot.dropwizard.guice.GuiceBundle;
import com.olacabs.jch.sdk.websockets.WebSocketClientManager;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.olacabs.jch.sdk.common.Constants;
import com.olacabs.jch.sdk.config.JchSDKConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JchSDKService extends Application<JchSDKConfiguration> {

    private GuiceBundle guiceBundle = GuiceBundle.<JchSDKConfiguration>newBuilder()
            .addModule(new JchSDKBinder())
            //.enableAutoConfig(getClass().getPackage().getName())
            .setConfigClass(JchSDKConfiguration.class)
            .build();

    public static void main(String[] args) throws Exception {
        new JchSDKService().run(args);
    }

    @Override
    public void initialize(final Bootstrap<JchSDKConfiguration> bootstrap) {
        bootstrap.addBundle(guiceBundle);
    }


    @Override
    public void run(JchSDKConfiguration jchSDKConfiguration, Environment environment) throws Exception {
        environment.lifecycle().manage(guiceBundle.getInjector().getInstance(SetupTools.class));
        environment.lifecycle().manage(guiceBundle.getInjector().getInstance(WebSocketClientManager.class));

//        InstanceDetails.getInstanceDetails().setInstanceId(UUID.randomUUID().toString());
    }

    @Override
    public String getName() {
        return Constants.APPLICATION_NAME;
    }

}
