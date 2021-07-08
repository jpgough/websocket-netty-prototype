package com.jpgough.websocket.config;

import com.jpgough.websocket.handler.DevNullHandler;
import com.jpgough.websocket.handler.EchoHandler;
import com.jpgough.websocket.handler.LargeFileHandler;
import com.jpgough.websocket.handler.SmallFileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class Routing {
    @Bean
    public HandlerMapping websocketHandlerMapping() {
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(getMappings());
        handlerMapping.setOrder(1);
        handlerMapping.setCorsConfigurations(Map.of("/**", new CorsConfiguration().applyPermitDefaultValues()));
        return handlerMapping;
    }

    Map<String, WebSocketHandler> getMappings() {
        Map<String, WebSocketHandler> mapping = new HashMap<>();

        mapping.put("/ack", new EchoHandler());
        mapping.put("/1mb", new SmallFileHandler());
        mapping.put("/100mb", new LargeFileHandler());
        mapping.put("/ignore", new DevNullHandler());

        return mapping;
    }
}
