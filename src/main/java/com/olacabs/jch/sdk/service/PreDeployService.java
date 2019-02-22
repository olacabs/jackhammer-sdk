package com.olacabs.jch.sdk.service;

import com.olacabs.jch.sdk.common.CustomErrorCodes;
import com.olacabs.jch.sdk.common.ExceptionMessages;
import com.olacabs.jch.sdk.exceptions.NoMatchingProviderException;
import com.olacabs.jch.sdk.spi.PreDeploySpi;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

@Slf4j
public class PreDeployService {

	private static PreDeployService preDeployService;
    private ServiceLoader<PreDeploySpi> preDeployServiceLoader;
    private Map<String, PreDeploySpi> providers;

    PreDeployService() {
        preDeployServiceLoader = ServiceLoader.load(PreDeploySpi.class);
        providers = new HashMap();
        Iterator<PreDeploySpi> deployProviders = preDeployServiceLoader.iterator();

        while (deployProviders.hasNext()) {
            PreDeploySpi deployProvider = deployProviders.next();
            providers.put(deployProvider.getToolName(),deployProvider);
        }
    }

    public static synchronized PreDeployService getInstance() {
        if (preDeployService == null) {
        	preDeployService = new PreDeployService();
        }
        return preDeployService;
    }

    public ProcessBuilder buildCommand(String command,String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", command);
        try {
            PreDeploySpi preDeployProvider = null;
            preDeployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", preDeployProvider.getToolName());
            ProcessBuilder buildCommand = preDeployProvider.buildCommand(command);
            return buildCommand;
        } catch (NullPointerException ne) {
            log.error("Error while building pre deploy command {} {}",toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }
    
    public ProcessBuilder buildCommand(String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", toolName);
        try {
            PreDeploySpi preDeployProvider = null;
            preDeployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", preDeployProvider.getToolName());
            ProcessBuilder buildCommand = preDeployProvider.buildCommand();
            return buildCommand;
        } catch (NullPointerException ne) {
            log.error("Error while building pre deploy command {} {}",toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }
    public int runCommand(ProcessBuilder command,String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", command);
        try {
            PreDeploySpi preDeployProvider = null;
            preDeployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", preDeployProvider.getToolName());
            int toolCommandResult = preDeployProvider.executeCommand(command);
            return toolCommandResult;
        } catch (NullPointerException ne) {
            log.error("Error while running pre deploy command {} {}",toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }
}
