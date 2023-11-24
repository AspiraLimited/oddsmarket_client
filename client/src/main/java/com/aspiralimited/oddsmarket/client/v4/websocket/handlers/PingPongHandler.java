package com.aspiralimited.oddsmarket.client.v4.websocket.handlers;

import com.neovisionaries.ws.client.WebSocket;
import org.json.JSONObject;

import java.time.Duration;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingPongHandler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final BlockingDeque<String> pongQueue = new LinkedBlockingDeque<>(10);

    private final WebSocket websocket;
    private final Duration pingInterval;
    private final Duration pongTimeout;

    private PongTimeoutListener pongTimeoutListener;

    public PingPongHandler(WebSocket websocket, Duration pingInterval, Duration pongTimeout) {
        this.websocket = websocket;
        this.pingInterval = pingInterval;
        this.pongTimeout = pongTimeout;
    }

    public synchronized void start() {
        scheduler.scheduleWithFixedDelay(this::run, pingInterval.toMillis(), pingInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void run() {
        String payload = String.valueOf(System.currentTimeMillis());

        JSONObject message = new JSONObject();
        message.put("cmd", "ping");
        message.put("msg", payload);
        websocket.sendText(message.toString());

        long deadline = System.currentTimeMillis() + pongTimeout.toMillis();

        while (!Thread.currentThread().isInterrupted()) {
            long timeout = deadline - System.currentTimeMillis();
            if (timeout <= 0) {
                onPongTimeout();
                return;
            }
            try {
                String receivedPayload = pongQueue.poll(timeout, TimeUnit.MILLISECONDS);
                if (receivedPayload == null) {
                    onPongTimeout();
                    return;
                } else if (payload.equals(receivedPayload)) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void onPongTimeout() {
        if (pongTimeoutListener != null) {
            pongTimeoutListener.onPongTimeout(websocket);
        }
    }

    public synchronized void stop() {
        scheduler.shutdownNow();
    }

    public void onMessage(JSONObject message) {
        if ("pong".equals(message.optString("cmd"))) {
            String payload = message.optString("msg");
            pongQueue.push(payload);
        }
    }

    public void setPongTimeoutListener(PongTimeoutListener pongTimeoutListener) {
        this.pongTimeoutListener = pongTimeoutListener;
    }
}
