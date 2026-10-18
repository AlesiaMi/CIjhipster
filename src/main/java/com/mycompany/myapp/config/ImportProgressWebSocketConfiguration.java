package com.mycompany.myapp.config;

import com.mycompany.myapp.web.websocket.ImportProgressWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ImportProgressWebSocketConfiguration implements WebSocketConfigurer {

    private final ImportProgressWebSocketHandler handler;

    public ImportProgressWebSocketConfiguration(ImportProgressWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/api/import/progress/ws");
    }
}
