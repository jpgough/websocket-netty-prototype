package com.jpgough.websocket.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class EchoHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        Flux<WebSocketMessage> incoming = webSocketSession.receive().doOnNext(WebSocketMessage::retain);
        incoming.map(WebSocketMessage::getPayloadAsText)
                .log();
        return webSocketSession.send(incoming);
    }
}
