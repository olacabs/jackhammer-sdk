package com.olacabs.jch.sdk.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Finding {
    String title;
    String description;
    String toolName;
    String fileName;
    String lineNumber;
    String code;
    String externalLink;
    String solution;
    String cvssScore;
    String cveCode;
    String cweCode;
    String location;
    String userInput;
    String advisory;
    String port;
    String protocol;
    String state;
    String product;
    String scripts;
    String version;
    String host;
    String request;
    String response;
    String severity;
    String fingerprint;
    long scanId;
    long repoId;
}
