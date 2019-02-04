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
    private long startTime;
    private long endTime;
    private String failedReasons; // null in case of success state
    private List<Finding> findings;
    private File resultFile;
    private long scanId;
    private long repoId;
    private String responseInstance;
    private long toolId;
    private Boolean sentFullList;
    @JsonIgnore
    private BufferedReader resultReader;
}
