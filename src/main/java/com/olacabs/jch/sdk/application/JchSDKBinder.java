package com.olacabs.jch.sdk.application;

import com.google.inject.AbstractModule;

import com.olacabs.jch.sdk.exceptions.handlers.ExceptionHandler;
import com.olacabs.jch.sdk.service.ScanService;
import com.olacabs.jch.sdk.utilities.ScanUtil;
import com.olacabs.jch.sdk.websockets.WebSocketClient;
import com.olacabs.jch.sdk.websockets.WebSocketClientManager;
import com.olacabs.jch.sdk.websockets.WebSocketClientTask;


public class JchSDKBinder extends AbstractModule {

    @Override
    protected void configure() {
        bind(ExceptionHandler.class);
        bind(WebSocketClientManager.class);
        bind(WebSocketClientTask.class);
        bind(ScanService.class);
        bind(WebSocketClient.class);
        bind(ScanUtil.class);
        bind(SetupTools.class);
    }
}
