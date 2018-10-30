package com.olacabs.jch.sdk.service;

import com.olacabs.jch.sdk.common.CustomErrorCodes;
import com.olacabs.jch.sdk.common.ExceptionMessages;
import com.olacabs.jch.sdk.exceptions.NoMatchingProviderException;
import com.olacabs.jch.sdk.spi.DeploySpi;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

@Slf4j
public class DeployService {

    private static DeployService deployService;
    private ServiceLoader<DeploySpi> deployServiceLoader;
    private Map<String, DeploySpi> providers;

    DeployService() {
        deployServiceLoader = ServiceLoader.load(DeploySpi.class);
        providers = new HashMap();
        Iterator<DeploySpi> deployProviders = deployServiceLoader.iterator();

        while (deployProviders.hasNext()) {
            DeploySpi deployProvider = deployProviders.next();
            providers.put(deployProvider.getToolName(), deployProvider);
        }
    }

    public static synchronized DeployService getInstance() {
        if (deployService == null) {
            deployService = new DeployService();
        }
        return deployService;
    }

    public ProcessBuilder buildCommand(String command, String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", command);
        try {
            DeploySpi deployProvider = null;
            deployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", deployProvider.getToolName());
            ProcessBuilder buildCommand = deployProvider.buildCommand(command);
            return buildCommand;
        } catch (NullPointerException ne) {
            log.error("Error while building deploy command {} {}", toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND, ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }

    public ProcessBuilder buildCommand(String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", toolName);
        try {
            DeploySpi deployProvider = null;
            deployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", deployProvider.getToolName());
            ProcessBuilder buildCommand = deployProvider.buildCommand();
            return buildCommand;
        } catch (NullPointerException ne) {
            log.error("Error while building deploy command {} {}", toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND, ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }

    public int runCommand(ProcessBuilder processBuilder, String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", processBuilder);
        try {
            DeploySpi deployProvider = null;
            deployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", deployProvider.getToolName());
            int toolCommandResult = deployProvider.executeCommand(processBuilder);
            return toolCommandResult;
        } catch (NullPointerException ne) {
            log.error("Error while running deploy command {} {}", toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND, ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }
}
