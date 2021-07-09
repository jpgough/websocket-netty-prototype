package com.jpgough.websocket.handler;

import com.jpgough.websocket.avro.AvroMessage;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.Stream;

public class AvroHandler implements WebSocketHandler {
    private final Random random = new Random();

    private static final int MAX_MESSAGE_BYTES = 1_024;

    private String configuredFilename;
    private Schema avroMessageSchema;

    private DatumWriter<AvroMessage> avroWriter;

    public AvroHandler(String filename) {
        this.configuredFilename = filename;
        avroMessageSchema = com.jpgough.websocket.avro.AvroMessage.getClassSchema();
        System.out.println(avroMessageSchema.toString());
        avroWriter = new SpecificDatumWriter<>(avroMessageSchema);
    }

    private File getFile(String filename) throws FileNotFoundException {
        return ResourceUtils.getFile("classpath:" + filename);
    }

    private Flux<ByteBuffer> getByteChunks(File file) throws IOException {
        // TODO: this can definitely be improved to lazily load the file, using Flux.using
        Sinks.Many<ByteBuffer> sink = Sinks.many().unicast().onBackpressureBuffer();
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] chunk = new byte[MAX_MESSAGE_BYTES];
        int chunkLength = 0;
        while ((chunkLength = fileInputStream.read(chunk)) != -1) {
            ByteBuffer buffer = ByteBuffer.allocate(chunkLength);
            buffer.put(chunk, 0, chunkLength);
            sink.tryEmitNext(buffer);
        }
        sink.tryEmitComplete();
        return sink.asFlux();
    }

    private ByteBuffer avroMessageToBytes(AvroMessage message) {
        try {
            return message.toByteBuffer();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Flux<WebSocketMessage> getChunkedMessages(String filename, WebSocketSession webSocketSession) throws IOException {
        File file = getFile(filename);
        int num_chunks = (int) Math.ceil((double) file.length() / MAX_MESSAGE_BYTES);
        return getByteChunks(file)
                .map(chunk -> {
                    var msg = AvroMessage.newBuilder()
                            .setBytes(chunk.rewind())
                            .setTotalMessages(num_chunks)
                            .build();
                    return msg;
                })
                .map(this::avroMessageToBytes)
                .map(bytes -> webSocketSession.binaryMessage(dataBufferFactory -> dataBufferFactory.wrap(bytes)));
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        try {
            return webSocketSession.send(getChunkedMessages(configuredFilename, webSocketSession));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
