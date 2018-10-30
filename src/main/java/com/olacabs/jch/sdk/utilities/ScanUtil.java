package com.olacabs.jch.sdk.utilities;

import com.olacabs.jch.sdk.common.Constants;
import com.olacabs.jch.sdk.common.CustomErrorCodes;
import com.olacabs.jch.sdk.exceptions.TempDirCreationException;
import com.olacabs.jch.sdk.common.ExceptionMessages;
import com.olacabs.jch.sdk.exceptions.AbstractException;
import com.olacabs.jch.sdk.exceptions.GitCloneException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@NoArgsConstructor
public class ScanUtil {

    public File createTempDirectory() throws TempDirCreationException {
        Path tempDirPath;
        try {
            tempDirPath = Files.createTempDirectory(Constants.TEMP_DIR_PREFIX);
        } catch (IOException io) {
            throw new TempDirCreationException(ExceptionMessages.TEMP_DIR_CREATION_ERROR, null, CustomErrorCodes.TEMP_DIR_CREATION_ERROR);
        }
        return tempDirPath.toFile();
    }

    public void cloneRepo(String target, File tmpDir) throws GitCloneException {
        try {
            StringBuilder gitCmd = new StringBuilder();
            gitCmd.append(target.replace("\"",""));
            gitCmd.append(Constants.EMPTY_STRING);
            gitCmd.append(tmpDir.getAbsolutePath().toString());
            Process process = Runtime.getRuntime().exec(gitCmd.toString());
            process.waitFor();
        } catch (Exception e) {
            log.error("Error while cloning repo" + e);
            throw new GitCloneException(ExceptionMessages.GIT_CLONE_ERROR, e, CustomErrorCodes.GIT_CLONE_ERROR);
        }
    }
}
