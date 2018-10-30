package com.olacabs.jch.sdk.websockets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import java.io.StringReader;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ScanRequestDecoder implements Decoder.Text<Map> {

    @Override
    public Map decode(String scanRequest) throws DecodeException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(scanRequest, new TypeReference<HashMap<String, String>>() {
            });
        } catch (Exception e) {
            log.info("Problem with Decoder: " + e.getMessage());
            return new HashMap();
        }
    }

    @Override
    public boolean willDecode(String scanObject) {
        try {
            JsonFactory factory = new JsonFactory();
            factory.createParser(new StringReader(scanObject));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void init(EndpointConfig config) {
        log.info("ScanRequestDecoder -init method called");
    }

    @Override
    public void destroy() {
        log.info("ScanRequestDecoder - destroy method called");
    }
}
