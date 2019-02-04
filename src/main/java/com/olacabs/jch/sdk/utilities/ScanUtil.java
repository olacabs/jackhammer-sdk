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
import java.time.Duration;
import java.util.concurrent.*;

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
            final Duration timeout = Duration.ofMinutes(5);
            ExecutorService executor = Executors.newSingleThreadExecutor();

            final Future<String> handler = executor.submit(new Callable() {
                @Override
                public String call() throws Exception {
                    log.info("started cloning the repo.....");
                    Process process = Runtime.getRuntime().exec(gitCmd.toString());
                    process.waitFor();
                    return "Success";
                }
            });
            try {
                handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                log.info("cloning completed.....");
            } catch (TimeoutException e) {
                handler.cancel(true);
                log.info("TimeoutException while cloning the repo.....");
            }
            executor.shutdownNow();
        } catch (Exception e) {
            log.error("Error while cloning repo" + e);
            throw new GitCloneException(ExceptionMessages.GIT_CLONE_ERROR, e, CustomErrorCodes.GIT_CLONE_ERROR);
        }
    }
}
