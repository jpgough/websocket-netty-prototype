package com.jpgough.websocket.avro;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.reflect.ReflectData;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class AvroMessageSchemaGenerator {
    public static void main(String[] args) {
        Schema avroMessageSchema = SchemaBuilder.record("AvroMessage")
                .namespace("com.jpgough.websocket.avro")
                .fields()
                .requiredInt("totalMessages")
                .requiredBytes("bytes")
                .endRecord();
        try(FileWriter writer = new FileWriter(ResourceUtils.getFile("classpath:AvroMessage.avsc"))) {
            writer.append(avroMessageSchema.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
