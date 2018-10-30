package com.olacabs.jch.sdk.service;

import com.olacabs.jch.sdk.common.CustomErrorCodes;
import com.olacabs.jch.sdk.common.ExceptionMessages;
import com.olacabs.jch.sdk.exceptions.NoMatchingProviderException;
import com.olacabs.jch.sdk.spi.PostDeploySpi;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

@Slf4j
public class PostDeployService {

    private static PostDeployService postDeployService;
    private ServiceLoader<PostDeploySpi> postDeployServiceLoader;
    private Map<String, PostDeploySpi> providers;

    PostDeployService() {
        postDeployServiceLoader = ServiceLoader.load(PostDeploySpi.class);
        providers = new HashMap();
        Iterator<PostDeploySpi> postDeployProviders = postDeployServiceLoader.iterator();
        while (postDeployProviders.hasNext()) {
            PostDeploySpi postDeployProvider = postDeployProviders.next();
            providers.put(postDeployProvider.getToolName(),postDeployProvider);
        }
    }

    public static synchronized PostDeployService getInstance() {
        if (postDeployService == null) {
            postDeployService = new PostDeployService();
        }
        return postDeployService;
    }

    public ProcessBuilder buildCommand(String command,String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", command);
        try {
            PostDeploySpi postDeployProvider = null;
            postDeployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", postDeployProvider.getToolName());
            ProcessBuilder processBuilder = postDeployProvider.buildCommand(command);
            return processBuilder;
        } catch (NullPointerException ne) {
            log.error("Error while building post deploy command");
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }

    public ProcessBuilder buildCommand(String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", toolName);
        try {
            PostDeploySpi postDeployProvider = null;
            postDeployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", postDeployProvider.getToolName());
            ProcessBuilder buildCommand = postDeployProvider.buildCommand();
            return buildCommand;
        } catch (NullPointerException ne) {
            log.error("Error while building pre deploy command {} {}",toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }

    public int runCommand(ProcessBuilder processBuilder,String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for {} - {}", processBuilder);
        try {
            PostDeploySpi postDeployProvider = null;
            postDeployProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for {} - {}", postDeployProvider.getToolName());
            int toolCommandResult = postDeployProvider.executeCommand(processBuilder);
            return toolCommandResult;
        } catch (NullPointerException ne) {
            log.error("Error while running post deploy command");
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }
}
