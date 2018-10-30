package com.olacabs.jch.sdk.websockets;

import com.google.inject.Inject;
import com.olacabs.jch.sdk.config.JchSDKConfiguration;
import io.dropwizard.lifecycle.Managed;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketClientManager implements Managed {

    @Inject
    WebSocketClientTask websocketClientTask;

    @Inject
    JchSDKConfiguration jchSDKConfiguration;

    public void start() throws Exception {
        int threadPoolSize = jchSDKConfiguration.getWebsocketClientConfiguration().getThreadPoolSize();
        int initialDelay = jchSDKConfiguration.getWebsocketClientConfiguration().getInitialDelay();
        int period = jchSDKConfiguration.getWebsocketClientConfiguration().getPeriod();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(threadPoolSize);
        executor.scheduleAtFixedRate(websocketClientTask, initialDelay, period, TimeUnit.MILLISECONDS.SECONDS);
    }

    public void stop() throws Exception {

    }
}
