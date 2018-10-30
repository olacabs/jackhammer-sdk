package com.olacabs.jch.sdk.spi;

import com.olacabs.jch.sdk.models.ScanResponse;

public interface ResultParserSpi {
    public ScanResponse parseResults(ScanResponse scanResponse);
    public String getToolName();
}
