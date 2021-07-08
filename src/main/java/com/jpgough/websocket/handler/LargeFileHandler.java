package com.jpgough.websocket.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

public class LargeFileHandler implements WebSocketHandler {
    private final Random random = new Random();

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        Flux<WebSocketMessage> messages = Flux.create(sink -> {
                    for (int msg = 0; msg < 10; msg++) {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < 10_000_000; i++) {
                            builder.append((char) ('a' + random.nextInt(26)));
                        }
                        sink.next(webSocketSession.textMessage(builder.toString()));
                    }
                }
        );
        return webSocketSession.send(messages);
    }
}
