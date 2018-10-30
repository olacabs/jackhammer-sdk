package com.olacabs.jch.sdk.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olacabs.jch.sdk.models.ScanResponse;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

@Slf4j
public class ScanResponseEncoder implements Encoder.Text<ScanResponse> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void init(EndpointConfig arg0) {
        // TODO Auto-generated method stub

    }

    public String encode(ScanResponse scanResponse) throws EncodeException {
        try {
            log.info("sending scan response for scan id");
            return MAPPER.writeValueAsString(scanResponse);
        } catch (IOException e) {
            throw new EncodeException(scanResponse, "Could not encode scan response", e);
        }
    }

}

