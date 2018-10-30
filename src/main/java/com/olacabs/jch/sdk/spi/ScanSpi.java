package com.olacabs.jch.sdk.spi;

import com.olacabs.jch.sdk.models.ScanRequest;
import com.olacabs.jch.sdk.models.ScanResponse;

public interface ScanSpi {
    public ProcessBuilder buildScanCommand(ScanRequest scanRequest);
    public ScanResponse executeScanCommand(ProcessBuilder command);
    public String getToolName();
}
