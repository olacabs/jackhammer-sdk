package com.olacabs.jch.sdk.service;

import com.olacabs.jch.sdk.common.CustomErrorCodes;
import com.olacabs.jch.sdk.common.ExceptionMessages;
import com.olacabs.jch.sdk.exceptions.NoMatchingProviderException;
import com.olacabs.jch.sdk.models.ScanRequest;
import com.olacabs.jch.sdk.models.ScanResponse;
import com.olacabs.jch.sdk.spi.ScanSpi;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

@Slf4j
public class ScanService {

    private static ScanService scanService;

    private ServiceLoader<ScanSpi> scanServiceLoader;
    private Map<String, ScanSpi> providers;

    ScanService() {
        scanServiceLoader = ServiceLoader.load(ScanSpi.class);
        providers = new HashMap<>();
        Iterator<ScanSpi> scanProviders = scanServiceLoader.iterator();
        while (scanProviders.hasNext()) {
            ScanSpi scanProvider = scanProviders.next();
            providers.put(scanProvider.getToolName(),scanProvider);
        }
    }

    public static synchronized ScanService getInstance() {
        if (scanService == null) {
            scanService = new ScanService();
        }
        return scanService;
    }


    public ProcessBuilder buildScanCommand(ScanRequest scanRequest, String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", toolName);
        try {
            ScanSpi scanProvider = null;
            scanProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", scanProvider.getToolName());
            ProcessBuilder buildCommand = scanProvider.buildScanCommand(scanRequest);
            return buildCommand;
        } catch (NullPointerException ne) {
            log.error("Error while building scan command {} {}",toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }

    public ScanResponse runScanCommand(ProcessBuilder command,String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", command);
        try {
            ScanSpi scanProvider = null;
            scanProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", scanProvider.getToolName());
            ScanResponse scanResponse = scanProvider.executeScanCommand(command);
            return scanResponse;
        } catch (NullPointerException ne) {
            log.error("Error while running scan command {} {}",toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }
}
