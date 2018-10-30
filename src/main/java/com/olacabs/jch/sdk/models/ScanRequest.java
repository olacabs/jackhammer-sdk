package com.olacabs.jch.sdk.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ScanRequest extends AbstractModel {
    private long scanId;
    private long repoId;
    private Boolean cloneRequire;
    private Boolean isMobileScan;
    private String target;
    private String supportedTools;
    private File resultFile;
}
