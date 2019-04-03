package com.olacabs.jch.sdk.websockets;

import com.google.inject.Inject;
import com.olacabs.jch.sdk.common.Constants;
import com.olacabs.jch.sdk.config.JchSDKConfiguration;

import com.olacabs.jch.sdk.models.JchTasksResponse;
import com.olacabs.jch.sdk.models.Task;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class WebSocketClientTask implements Runnable {

    @Inject
    JchSDKConfiguration jchSDKConfiguration;

    private static Session session;

    public static void setSession(Session session) {
        WebSocketClientTask.session = session;
    }

    public static Session getSession() {
        return WebSocketClientTask.session;
    }

    public void run() {
        if (WebSocketClientTask.getSession() == null) connectWebSocketServer();
    }

    private void connectWebSocketServer() {
        try {
            ClientManager client = ClientManager.createClient();
            String isLocalHost = System.getenv(Constants.LOCAL_SETUP);
            if(StringUtils.equals(Constants.TRUE,isLocalHost)) {
                log.info("Connecting to web socket local server......{}");
                Session session = client.connectToServer(WebSocketClient.class, new URI(jchSDKConfiguration.getWebsocketClientConfiguration().getLocalServer() + Constants.WEB_SOCKET_API));
                session.setMaxIdleTimeout(0);
                WebSocketClientTask.setSession(session);
                log.info("Connected to local web socket server ");
            } else {
                JchTasksResponse jchTasksResponse = getWebSocketServerResponse();
                for (Task task : jchTasksResponse.getTasks()) {
                    StringBuilder webSocketServer = new StringBuilder();
                    webSocketServer.append(Constants.WEB_SOCKET_HOST_PREFIX);
                    webSocketServer.append(task.getHost());
                    webSocketServer.append(Constants.COLON);
                    webSocketServer.append(task.getPorts().get(0));
                    Session session = client.connectToServer(WebSocketClient.class, new URI(webSocketServer.toString() + Constants.WEB_SOCKET_API));
                    session.setMaxIdleTimeout(0);
                    WebSocketClientTask.setSession(session);
                    log.info("Connected to web socket server ");
                    break;
                }
            }
        } catch (DeploymentException | URISyntaxException e) {
            log.info("DeploymentException or server is not running ....",e);
        } catch (Exception e) {
            log.info("Exception or server is not running ....",e);
        } catch (Throwable th) {
            log.info("Throwable Exception or server is not running ....",th);
        }
    }

    private JchTasksResponse getWebSocketServerResponse() {
        String serverHost = System.getenv(Constants.SERVER_HOST);
//        String serverHost = jchSDKConfiguration.getWebsocketClientConfiguration().getServerHost();
        WebTarget webTarget = ClientBuilder.newClient().target(serverHost + Constants.JCH_TASKS_API);
        Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
        JchTasksResponse jchTasksResponse = response.readEntity(new GenericType<JchTasksResponse>() {
        });
        return jchTasksResponse;
    }

}
