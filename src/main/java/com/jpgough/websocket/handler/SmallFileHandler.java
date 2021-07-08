package com.jpgough.websocket.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;

public class SmallFileHandler implements WebSocketHandler {
    private final Random random = new Random();
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1_000_000; i++) {
            builder.append((char)('a' + random.nextInt(26)));
        }
        return webSocketSession.send(Flux.just(webSocketSession.textMessage(builder.toString())));
    }
}
