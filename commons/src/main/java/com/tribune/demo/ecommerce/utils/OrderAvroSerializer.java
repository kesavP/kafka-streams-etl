package com.tribune.demo.ecommerce.utils;

import com.tribune.demo.ecommerce.domain.avro.order.OrderValue;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OrderAvroSerializer implements Serializer<OrderValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAvroSerializer.class);

    @Override
    public void configure(java.util.Map<String, ?> configs, boolean isKey) {
        // No configuration required for pure Avro binary encoding
    }

    @Override
    public byte[] serialize(String topic, OrderValue data) {
        if (data == null) {
            LOGGER.debug("Null data to serialize for topic {}", topic);
            return null;
        }

        DatumWriter<OrderValue> writer = new SpecificDatumWriter<>(OrderValue.class);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Error serializing OrderValue to Avro for topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize OrderValue", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}
