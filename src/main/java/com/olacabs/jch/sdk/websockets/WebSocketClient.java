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
import com.olacabs.jch.sdk.models.ScanRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
        session.getContainer().setAsyncSendTimeout(60000*5);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.SUPPORTED_PLATFORM_KEY, System.getenv(Constants.SUPPORTED_PLATFORM_VALUE));
            jsonObject.put(Constants.TOOL_ID_KEY, System.getenv(Constants.TOOL_ID_VALUE));
            jsonObject.put(Constants.TOOL_RESPONSE_INSTANCE_KEY, Constants.TOOL_RESPONSE_INSTANCE_VALUE);
            jsonObject.put(Constants.MAX_ALLOWED_SCANS_KEY, System.getenv(Constants.MAX_ALLOWED_SCANS_VALUE));
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
        ScanResponse scanResponse = sendScanRequest(scanRequest);
        sendScanResponseInChunks(WebSocketClientTask.getSession(), scanResponse);
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
            if (StringUtils.equals(cloneRequired,Constants.TRUE)) {
                File targetDir = scanUtil.createTempDirectory();
                scanUtil.cloneRepo(scanNode.get(Constants.TARGET).toString(), targetDir);
                scanRequest.setTarget(targetDir.getAbsolutePath());
                scanRequest.setCloneRequire(true);
                scanRequest.setIsMobileScan(false);
            } else {
                if (StringUtils.equals(scanNode.get(Constants.IS_MOBILE_SCAN).toString(), Constants.TRUE)) {
                    InputStream apkFileFromS3 = getApkFileFromS3(scanNode.get(Constants.APK_TEMP_FILE).toString().replace("\"",""));
                    Path tempFile = getTempFile();
                    Files.copy(apkFileFromS3,tempFile);
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
            log.error("Building scan request or git clone error....", e);
            return scanRequest;
        } catch (GitCloneException gc) {
            log.error("GitCloneException...." ,gc);
            return scanRequest;
        } catch (TempDirCreationException tdc) {
            log.error("TempDirCreationException....",tdc);
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
            if(scanRequest.getIsMobileScan()) {
                File apkFile = new File(scanRequest.getTarget());
                apkFile.delete();
            }
            if(scanRequest.getCloneRequire()) {
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
        try {
            session.getBasicRemote().sendObject(scanResponse);
//            long responseSize =  ObjectSizeCalculator.getObjectSize(scanResponse);
//            if (responseSize < 65536) {
//                scanResponse.setToolId(Long.valueOf(System.getenv(Constants.TOOL_ID_VALUE)));
//                session.getBasicRemote().sendObject(scanResponse);
//            } else {
//                long totalObjectSize = 0;
//                long chunkTimes = 0;
//                List<Finding> findingList = scanResponse.getFindings();
//                List<Finding> chunkList = new ArrayList<>();
//                for (Finding finding : findingList) {
//                    long currentObjectSize = ObjectSizeCalculator.getObjectSize(finding);
//                    long cumulativeObjectSize = currentObjectSize + totalObjectSize;
//                    if (cumulativeObjectSize > 65536) {
//                        scanResponse.setFindings(chunkList);
//                        session.getBasicRemote().sendObject(scanResponse);
//                        totalObjectSize = 0;
//                        chunkList.clear();
//                        chunkList.add(finding);
//                        chunkTimes = chunkTimes + 1;
//                    } else {
//                        chunkList.add(finding);
//                        totalObjectSize += currentObjectSize;
//                    }
//                }
//                scanResponse.setFindings(chunkList);
//                scanResponse.setToolId(Long.valueOf(System.getenv(Constants.TOOL_ID_VALUE)));
//                session.getBasicRemote().sendObject(scanResponse);
//            }
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
            log.error("Error while fetching s3 apk file",e);
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
