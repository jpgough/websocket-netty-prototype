package com.jpgough.websocket.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class BytesHandler implements WebSocketHandler {
    private final Random random = new Random();
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        return webSocketSession.send(Flux.just(webSocketSession.binaryMessage(dataBufferFactory ->
                dataBufferFactory.wrap("Hello".getBytes(StandardCharsets.UTF_8)))));
    }
}
