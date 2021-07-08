package com.jpgough.websocket.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DevNullHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        return webSocketSession.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .log()
                .then();
    }
}
