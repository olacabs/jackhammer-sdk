package com.olacabs.jch.sdk.application;

import com.olacabs.jch.sdk.common.Constants;
import com.olacabs.jch.sdk.exceptions.NoMatchingProviderException;
import com.olacabs.jch.sdk.service.DeployService;
import com.olacabs.jch.sdk.service.PostDeployService;
import com.olacabs.jch.sdk.service.PreDeployService;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SetupTools implements Managed {

    public void start() throws Exception {
        try {
            String toolName = System.getenv(Constants.TOOL_NAME);
            log.info("current Sdk tool name {} - {}", toolName);

            //pre deploy
            PreDeployService preDeployService = PreDeployService.getInstance();
            ProcessBuilder preDeployProcessBuilder = preDeployService.buildCommand(toolName);
            preDeployService.runCommand(preDeployProcessBuilder, toolName);

            //deploy
            DeployService deployService = DeployService.getInstance();
            ProcessBuilder deployProcessBuilder = deployService.buildCommand(toolName);
            deployService.runCommand(deployProcessBuilder,toolName);

            //post deploy
            PostDeployService postDeployService = PostDeployService.getInstance();
            ProcessBuilder postDeployProcessBuilder = postDeployService.buildCommand(toolName);
            postDeployService.runCommand(postDeployProcessBuilder,toolName);

        } catch (NoMatchingProviderException e) {
            log.error("Error while installing tool ",e);
        }
    }

    public void stop() throws Exception {

    }
}
