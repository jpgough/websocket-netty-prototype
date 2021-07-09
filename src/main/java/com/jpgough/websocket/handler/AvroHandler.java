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
//        avroMessageSchema = ReflectData.get().getSchema(AvroMessage.class);
//        avroMessageSchema = SchemaBuilder.record("AvroMessage")
//                .namespace("com.jpgough.websocket.avro")
//                .fields()
//                .requiredInt("totalMessages")
//                .requiredBytes("bytes")
//                .endRecord();
        avroMessageSchema = com.jpgough.websocket.avro.AvroMessage.getClassSchema();
        System.out.println(avroMessageSchema.toString());
        avroWriter = new SpecificDatumWriter<>(avroMessageSchema);

//        try {
//            File file = ResourceUtils.getFile("classpath:" + filename);
//            binaryFile = Files.readAllBytes(file.toPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private File getFile(String filename) throws FileNotFoundException {
        return ResourceUtils.getFile("classpath:" + filename);
    }

    private Flux<ByteBuffer> getByteChunks(File file) throws IOException {
        // TODO: this can definitely be improved to lazily load the file
        Sinks.Many<ByteBuffer> sink = Sinks.many().unicast().onBackpressureBuffer();
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] chunk = new byte[MAX_MESSAGE_BYTES];
        int chunkLength = 0;
        while ((chunkLength = fileInputStream.read(chunk)) != -1) {
//            System.out.println(Arrays.toString(Base64.getEncoder().encode(chunk)));
            ByteBuffer buffer = ByteBuffer.allocate(chunkLength);
            buffer.put(chunk, 0, chunkLength);
//            chunk = "Hello".getBytes(StandardCharsets.UTF_8);
//            ByteBuffer buffer = ByteBuffer.allocate(chunk.length);
//            buffer.put(chunk);
            sink.tryEmitNext(buffer);
        }
        return sink.asFlux();
    }

    private Flux<ByteBuffer> getByteChunks2(Path path) {
        return Flux.using(
                () -> Files.lines(path),
                stringStream -> Flux.fromStream(stringStream)
                        .map(String::getBytes)
                        .map(bytes -> ByteBuffer.allocate(bytes.length)
                                .put(bytes)),
                Stream::close
        );
    }

    private ByteBuffer avroMessageToBytes(AvroMessage message) {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        Encoder encoder;
//        try {
//            encoder = EncoderFactory.get().jsonEncoder(avroMessageSchema, stream);
////            encoder = EncoderFactory.get().binaryEncoder();
//
//            avroWriter.write(message, encoder);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//        byte[] output = stream.toByteArray();
//        System.out.println(Arrays.toString(output));
//        return stream.toByteArray();
        try {
            return message.toByteBuffer();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Flux<WebSocketMessage> getChunkedMessages(String filename, WebSocketSession webSocketSession) throws IOException {
//        Path path = new ClassPathResource(filename).getFile().toPath();
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
