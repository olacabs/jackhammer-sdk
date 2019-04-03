package com.olacabs.jch.sdk.websockets;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.olacabs.jch.sdk.common.Constants;
import com.olacabs.jch.sdk.config.JchSDKConfiguration;
import com.olacabs.jch.sdk.exceptions.GitCloneException;
import com.olacabs.jch.sdk.exceptions.NoMatchingProviderException;
import com.olacabs.jch.sdk.exceptions.TempDirCreationException;
import com.olacabs.jch.sdk.models.Finding;
import com.olacabs.jch.sdk.models.ScanRequest;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.websocket.*;

import com.olacabs.jch.sdk.models.ScanResponse;
import com.olacabs.jch.sdk.service.ResultParserService;
import com.olacabs.jch.sdk.service.ScanService;
import com.olacabs.jch.sdk.utilities.ScanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

@Slf4j
@ClientEndpoint(decoders = {ScanRequestDecoder.class}, encoders = {ScanResponseEncoder.class})
public class WebSocketClient {

    @Inject
    JchSDKConfiguration jchSDKConfiguration;

    ScanUtil scanUtil = new ScanUtil();

    @OnOpen()
    public void onOpen(Session session) {
        WebSocketClientTask.setSession(session);
        session.getContainer().setDefaultMaxSessionIdleTimeout(0);
        session.setMaxTextMessageBufferSize(100000000);
        session.getContainer().setAsyncSendTimeout(60000 * 5);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.SUPPORTED_PLATFORM_KEY, System.getenv(Constants.SUPPORTED_PLATFORM_VALUE));
            jsonObject.put(Constants.TOOL_ID_KEY, System.getenv(Constants.TOOL_ID_VALUE));
            jsonObject.put(Constants.TOOL_RESPONSE_INSTANCE_KEY, Constants.TOOL_RESPONSE_INSTANCE_VALUE);
            jsonObject.put(Constants.MAX_ALLOWED_SCANS_KEY, System.getenv(Constants.MAX_ALLOWED_SCANS_VALUE));
            jsonObject.put(Constants.HOSTNAME, System.getenv(Constants.ENV_HOSTNAME));
            jsonObject.put(Constants.PORT, System.getenv(Constants.ENV_PORTS));
            session.getBasicRemote().sendObject(jsonObject.toString());
            log.info("Tool info has sent");
        } catch (IOException io) {
            log.error("IOException on onConnect", io);
        } catch (EncodeException ee) {
            log.error("EncodeException on onConnect", ee);
        }
        log.info("Connected ... " + session.getId());
    }

    @OnMessage(maxMessageSize = 100000000)
    public void onMessage(String requestObject) {
        ScanRequest scanRequest = buildScanRequest(requestObject);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ScanResponse scanResponse = sendScanRequest(scanRequest);
                sendScanResponseInChunks(WebSocketClientTask.getSession(), scanResponse);
            }
        }).start();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        WebSocketClientTask.setSession(null);
        log.info("Websocket max idle  time " + session.getMaxIdleTimeout());
        log.info(String.format("Session %s close because of %s", session.getId(), closeReason));
    }

    private ScanRequest buildScanRequest(String scanObject) {
        ScanRequest scanRequest = new ScanRequest();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode scanNode = mapper.readTree(scanObject);
            String cloneRequired = scanNode.get(Constants.CLONE_REQUIRED).toString();
            if (StringUtils.equals(cloneRequired, Constants.TRUE)) {
                File targetDir = scanUtil.createTempDirectory();
                scanRequest.setGitTarget(scanNode.get(Constants.TARGET).toString());
                scanUtil.cloneRepo(scanNode.get(Constants.TARGET).toString(), targetDir);
                scanRequest.setTarget(targetDir.getAbsolutePath());
                scanRequest.setCloneRequire(true);
                scanRequest.setIsMobileScan(false);
            } else {
                if (StringUtils.equals(scanNode.get(Constants.IS_MOBILE_SCAN).toString(), Constants.TRUE)) {
                    InputStream apkFileFromS3 = getApkFileFromS3(scanNode.get(Constants.APK_TEMP_FILE).toString().replace("\"", ""));
                    Path tempFile = getTempFile();
                    Files.copy(apkFileFromS3, tempFile);
                    scanRequest.setTarget(tempFile.toAbsolutePath().toString());
                    apkFileFromS3.close();
                    scanRequest.setIsMobileScan(true);
                    scanRequest.setCloneRequire(false);
                } else {
                    scanRequest.setTarget(scanNode.get(Constants.TARGET).toString());
                    scanRequest.setIsMobileScan(false);
                    scanRequest.setCloneRequire(false);
                }
            }
            scanRequest.setScanId(scanNode.get(Constants.ID).asLong());
            scanRequest.setRepoId(scanNode.get(Constants.REPO_ID).asLong());
            return scanRequest;
        } catch (IOException e) {
            log.error("Building scan request or git clone error...." + e);
            return scanRequest;
        } catch (GitCloneException e) {
            log.error("Building scan request or git clone error...." + e);
            return scanRequest;
        } catch (TempDirCreationException e) {
            log.error("Building scan request or git clone error...." + e);
            return scanRequest;
        }
    }

    private ScanResponse sendScanRequest(ScanRequest scanRequest) {
        ScanResponse scanResponse = new ScanResponse();
        String toolName = System.getenv(Constants.TOOL_NAME);
        try {
            ScanService scanService = ScanService.getInstance();
            ResultParserService resultParserService = ResultParserService.getInstance();
            ProcessBuilder processBuilder = scanService.buildScanCommand(scanRequest, toolName);
            scanResponse = scanService.runScanCommand(processBuilder, toolName);
            scanResponse.setResultFile(scanRequest.getResultFile());
            scanResponse.setScanId(scanRequest.getScanId());
            scanResponse.setRepoId(scanRequest.getRepoId());
            scanResponse = resultParserService.parseScanResults(scanResponse, toolName);
            if (scanRequest.getIsMobileScan()) {
                File apkFile = new File(scanRequest.getTarget());
                apkFile.delete();
            }
            if (scanRequest.getCloneRequire()) {
                FileUtils.deleteDirectory(new File(scanRequest.getTarget()));
            }
        } catch (NoMatchingProviderException e) {
            scanResponse.setStatus(Constants.FAILED_STATUS);
            scanResponse.setFailedReasons("Configured tool is not loaded");
            log.error("NoMatching ProviderException ", e);
        } catch (IOException io) {
            log.error("Error while deleting target directory");
        }
        scanResponse.setEndTime(System.currentTimeMillis());
        scanResponse.setResponseInstance(Constants.SCAN_RESPONSE_INSTANCE_VALUE);
        return scanResponse;
    }

    private void sendScanResponseInChunks(Session session, ScanResponse scanResponse) {
        scanResponse.setToolId(Long.valueOf(System.getenv(Constants.TOOL_ID_VALUE)));
        int batchSize = 20;
        try {
//            session.getBasicRemote().sendObject(scanResponse);
            if (scanResponse.getFindings() == null) {
                scanResponse.setSentFullList(true);
                session.getBasicRemote().sendObject(scanResponse);
                return;
            }
            if (scanResponse.getFindings().size() < batchSize) {
                log.info("Records less than 20,sending all records for scan id...{}..{}", scanResponse.getScanId());
                scanResponse.setSentFullList(true);
                session.getBasicRemote().sendObject(scanResponse);
            } else {
                int count = 0;
                int totalCount = scanResponse.getFindings().size();
                int sentCount = 0;
                log.info("Total findings for scan id...{}..{}", scanResponse.getScanId());
                List<Finding> findingList = scanResponse.getFindings();
                List<Finding> chunkList = new ArrayList();
                for (Finding finding : findingList) {
                    if (count == batchSize) {
                        scanResponse.setFindings(chunkList);
                        log.info("sending 20 of all records for scan id...{}..{}", scanResponse.getScanId());
                        if (sentCount == totalCount) scanResponse.setSentFullList(true);
                        session.getBasicRemote().sendObject(scanResponse);
                        chunkList.clear();
                        chunkList.add(finding);
                        count = 0;
                    } else {
                        chunkList.add(finding);
                        count += 1;
                    }
                    sentCount += 1;
                }
                if (chunkList.size() > 0) {
                    log.info("sending rest of records for scan id...{}..{}", scanResponse.getScanId());
                    scanResponse.setFindings(chunkList);
                    scanResponse.setSentFullList(true);
                    session.getBasicRemote().sendObject(scanResponse);
                }
            }
        } catch (EncodeException e) {
            log.error("EncodeException while sending response ", e);
        } catch (IOException e) {
            log.error("IOException while sending response ", e);
        } catch (Exception e) {
            log.error("Exception while sending response ", e);
        }
    }

    //
//    private InputStream getApkFileFromS3() {
//
//    }
    public InputStream getApkFileFromS3(String apkFileName) {
        InputStream inputStream = null;
        try {
            String S3bucketFile = Constants.S3_APK_FOLDER_NAME + Constants.FILE_SEPARATOR + apkFileName;
            S3Object s3object = getS3Client().getObject(getBucketName(), S3bucketFile);
            inputStream = s3object.getObjectContent();
        } catch (Throwable e) {
            log.error("Error while fetching s3 apk file", e);
        }
        return inputStream;
    }

    private AmazonS3 getS3Client() {
        String accessKey = System.getenv(Constants.S3_ACCESS_KEY);
        String secretKey = System.getenv(Constants.S3_SECRET_KEY);
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().
                withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(System.getenv(Constants.S3_REGION))
                .build();
        return s3Client;
    }

    private String getBucketName() {
        return System.getenv(Constants.S3_BUCKET);
    }


    private Path getTempFile() {
        String fileExtension = Constants.APK_FILE_FORMAT;
        long millis = System.currentTimeMillis();
        String tmpdir = System.getProperty(Constants.TEMP_DIR_PROPERTY);
        String tmpFile = millis + fileExtension;
        Path destinationPath = FileSystems.getDefault().getPath(tmpdir, tmpFile);
        return destinationPath;
    }
}
