package com.olacabs.jch.sdk.spi;

import java.io.File;

public interface BaseSpi {


    public ProcessBuilder buildCommand();

    public ProcessBuilder buildCommand(File scriptFile);

    public ProcessBuilder buildCommand(String command);

    public int executeCommand(ProcessBuilder command);

    public String getToolName();
}
