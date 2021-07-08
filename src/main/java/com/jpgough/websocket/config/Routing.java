package com.jpgough.websocket.config;

import com.jpgough.websocket.handler.EchoHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Routing {
    @Bean
    public HandlerMapping websocketHandlerMapping() {
        Map<String, WebSocketHandler> mapping = new HashMap<>();
        mapping.put("/echo", new EchoHandler());

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(mapping);
        handlerMapping.setOrder(1);
        return  handlerMapping;
    }
}
