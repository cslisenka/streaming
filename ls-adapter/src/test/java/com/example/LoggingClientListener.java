package com.example;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.ConnectionDetails;
import com.lightstreamer.client.ConnectionOptions;
import com.lightstreamer.client.LightstreamerClient;
import org.apache.log4j.Logger;

public class LoggingClientListener implements ClientListener {

    private static final Logger log = Logger.getLogger(LoggingClientListener.class);

    private final LightstreamerClient client;

    public LoggingClientListener(LightstreamerClient client) {
        this.client = client;
    }

    @Override
    public void onListenEnd(LightstreamerClient ls) {
        log.info("onListenEnd");
    }

    @Override
    public void onListenStart(LightstreamerClient ls) {
        log.info("onListenStart");
    }

    @Override
    public void onServerError(int i, String errorMessage) {
        log.info("onServerError " + errorMessage);
    }

    @Override
    public void onStatusChange(String status) {
        log.info("onStatusChange " + status);
        if (status.contains("CONNECTED")) {
            log(client);
        }
    }

    @Override
    public void onPropertyChange(String property) {
        switch (property) {
            case "serverSocketName":
                log.info("onPropertyChange " + property + "=" + client.connectionDetails.getServerSocketName());
            break;
            case "adapterSet":
                log.info("onPropertyChange " + property + "=" + client.connectionDetails.getAdapterSet());
            break;
            case "clientIp":
                log.info("onPropertyChange " + property + "=" + client.connectionDetails.getClientIp());
                break;
            case "serverAddress":
                log.info("onPropertyChange " + property + "=" + client.connectionDetails.getServerAddress());
                break;
            case "serverInstanceAddress":
                log.info("onPropertyChange " + property + "=" + client.connectionDetails.getServerInstanceAddress());
                break;
            case "sessionId":
                log.info("onPropertyChange " + property + "=" + client.connectionDetails.getSessionId());
                break;
            case "user":
                log.info("onPropertyChange " + property + "=" + client.connectionDetails.getUser());
                break;
            case "realMaxBandwidth":
                log.info("onPropertyChange " + property + "=" + client.connectionOptions.getRealMaxBandwidth());
                break;
            case "keepaliveInterval":
                log.info("onPropertyChange " + property + "=" + client.connectionOptions.getKeepaliveInterval());
                break;
            default:
                log.info("onPropertyChange " + property);
            break;
        }
    }

    private void log(LightstreamerClient ls) {
        ConnectionOptions options = ls.connectionOptions;
        ConnectionDetails details = ls.connectionDetails;

        log.info("========== CONNECTION OPTIONS ==========");

        // Bandwidth
        log.info("BANDWIDTH: " +
            "realMaxBandwidth=" + options.getRealMaxBandwidth() + ", " +
            "requestedMaxBandwidth=" + options.getRequestedMaxBandwidth());

        // Intervals
        log.info("INTERVALS: " +
            "keepaliveInterval=" + options.getKeepaliveInterval() + ", " +
            "pollingInterval=" + options.getPollingInterval() + ", " +
            "reversedHeartbeatInterval=" + options.getReverseHeartbeatInterval());

        // Delays
        log.info("DELAYS: " +
            "firstRetryMaxDelay=" + options.getFirstRetryMaxDelay() + ", " +
            "retryDelay=" + options.getRetryDelay());

        // Timeouts
        log.info("TIMEOUTS: " +
            "connectTimeout=" + options.getConnectTimeout() + ", " +
            "reconnectTimeout=" + options.getReconnectTimeout() + ", " +
            "currentConnectTimeout=" + options.getCurrentConnectTimeout() + ", " +
            "forceBindTimeout=" + options.getForceBindTimeout() + ", " +
            "idleTimeout=" + options.getIdleTimeout() + ", " +
            "stalledTimeout=" + options.getStalledTimeout() + ", " +
            "switchCheckTimeout=" + options.getSwitchCheckTimeout());

        // Other properties
        log.info("OTHER: " +
            "forcedTransport=" + options.getForcedTransport() + ", " +
            "contentLength=" + options.getContentLength() + ", " +
            "httpExtraHeaders=" + options.getHttpExtraHeaders());

        log.info("DETAILS: " +
            "user=" + details.getUser() +
            "sessionId=" + details.getSessionId() + ", " +
            "serverInstanceAddress=" + details.getServerInstanceAddress() + ", " +
            "serverAddress=" + details.getServerAddress() + ", " +
            "clientIp=" + details.getClientIp() + ", " +
            "adapterSet=" + details.getAdapterSet() + ", " +
            "serverSocketName=" + details.getServerSocketName());

        log.info("========== END OF CONNECTION OPTIONS ==========");
    }
}
