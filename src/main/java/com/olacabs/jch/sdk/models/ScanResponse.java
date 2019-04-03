package com.olacabs.jch.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.File;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScanResponse {
    private String status;
    private String failedReasons; // null in case of success state
    private String responseInstance;
    private List<Finding> findings;
    private File resultFile;
    private Boolean sentFullList;
    private long startTime;
    private long endTime;
    private long scanId;
    private long repoId;
    private long toolId;

    @JsonIgnore
    private BufferedReader resultReader;
}
