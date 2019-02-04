package com.olacabs.jch.sdk.service;

import com.olacabs.jch.sdk.common.CustomErrorCodes;
import com.olacabs.jch.sdk.common.ExceptionMessages;
import com.olacabs.jch.sdk.exceptions.NoMatchingProviderException;
import com.olacabs.jch.sdk.models.ScanResponse;
import com.olacabs.jch.sdk.spi.ResultParserSpi;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

@Slf4j
public class ResultParserService {

    private static ResultParserService resultParserService;

    private ServiceLoader<ResultParserSpi> resultParserServiceLoader;
    private Map<String, ResultParserSpi> providers;

    ResultParserService() {
        resultParserServiceLoader = ServiceLoader.load(ResultParserSpi.class);
        providers = new HashMap<>();
        Iterator<ResultParserSpi> parserProviders = resultParserServiceLoader.iterator();
        while (parserProviders.hasNext()) {
            ResultParserSpi parserProvider = parserProviders.next();
            providers.put(parserProvider.getToolName(),parserProvider);
        }
    }

    public static synchronized ResultParserService getInstance() {
        if (resultParserService == null) {
            resultParserService = new ResultParserService();
        }
        return resultParserService;
    }


    public ScanResponse parseScanResults(ScanResponse scanResponse, String toolName) throws NoMatchingProviderException {
        log.info("trying the service loading for ResultParserService  {} - {}", toolName);
        try {
            ResultParserSpi parserProvider = null;
            parserProvider = providers.get(toolName);
            log.info("iterator is at  the service loading for ResultParserService {} - {}", parserProvider.getToolName());
            ScanResponse parsedScanResponse = parserProvider.parseResults(scanResponse);
            return parsedScanResponse;
        } catch (NullPointerException ne) {
            log.error("spi is not loaded for result parser, thrown NullPointerException for {} {}",toolName);
            throw new NoMatchingProviderException(ExceptionMessages.PROVIDER_NOT_FOUND,ne, CustomErrorCodes.PROVIDER_NOT_FOUND);
        }
    }
}
