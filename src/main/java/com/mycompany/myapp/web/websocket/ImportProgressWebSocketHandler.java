package com.mycompany.myapp.web.websocket;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class ImportProgressWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ImportProgressWebSocketHandler.class);
    private static final int MAX_AUTH_MESSAGE_SIZE = 16 * 1024;
    private final JwtDecoder jwtDecoder;
    private final ObjectMapper mapper;
    private final Map<String, Client> connections = new ConcurrentHashMap<>();
    private static final int AUTH_TIMEOUT_SECONDS = 10;

    private final ScheduledExecutorService authTimeoutExecutor = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "import-progress-auth-timeout");
        thread.setDaemon(true);
        return thread;
    });

    public ImportProgressWebSocketHandler(JwtDecoder jwtDecoder, ObjectMapper mapper) {
        this.jwtDecoder = jwtDecoder;
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(MAX_AUTH_MESSAGE_SIZE);

        connections.put(session.getId(), new Client(null, new ConcurrentWebSocketSessionDecorator(session, 5000, 64 * 1024)));

        authTimeoutExecutor.schedule(
            () -> {
                Client client = connections.get(session.getId());

                if (client != null && client.login() == null && connections.remove(session.getId(), client)) {
                    try {
                        if (session.isOpen()) {
                            session.close(CloseStatus.POLICY_VIOLATION);
                        }
                    } catch (IOException exception) {
                        LOG.debug("Could not close unauthenticated import progress WebSocket");
                    }
                }
            },
            AUTH_TIMEOUT_SECONDS,
            TimeUnit.SECONDS
        );
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Client client = connections.get(session.getId());
        if (client == null || client.login() != null || message.getPayloadLength() > MAX_AUTH_MESSAGE_SIZE) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        try {
            JsonNode payload = mapper.readTree(message.getPayload());
            if (!"authenticate".equals(payload.path("type").asText())) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            String token = payload.path("token").asText();

            String login = jwtDecoder.decode(token).getSubject();

            if (login == null || login.isBlank()) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            Client authenticated = new Client(login, client.session());

            if (!connections.replace(session.getId(), client, authenticated)) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            authenticated.session().sendMessage(new TextMessage("{\"type\":\"ready\"}"));
        } catch (Exception exception) {
            LOG.debug("Rejected import progress WebSocket authentication");
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    public void sendToUser(String login, Object event) {
        if (login == null) {
            return;
        }

        String payload;

        try {
            payload = mapper.writeValueAsString(event);
        } catch (Exception exception) {
            LOG.warn("Could not serialize import progress event", exception);
            return;
        }

        for (Map.Entry<String, Client> entry : connections.entrySet()) {
            Client client = entry.getValue();
            if (login.equals(client.login()) && client.session().isOpen()) {
                try {
                    client.session().sendMessage(new TextMessage(payload));
                } catch (Exception exception) {
                    connections.remove(entry.getKey(), client);

                    LOG.debug("Import progress WebSocket closed during send");
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        connections.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        connections.remove(session.getId());

        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @PreDestroy
    public void shutdownAuthTimeoutExecutor() {
        authTimeoutExecutor.shutdownNow();
    }

    private record Client(String login, WebSocketSession session) {}
}
