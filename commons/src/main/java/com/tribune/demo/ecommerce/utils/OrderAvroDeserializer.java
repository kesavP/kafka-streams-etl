package com.tribune.demo.ecommerce.utils;

import com.tribune.demo.ecommerce.domain.avro.order.OrderValue;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OrderAvroDeserializer implements Deserializer<OrderValue> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderAvroDeserializer.class);

    @Override
    public void configure(java.util.Map<String, ?> configs, boolean isKey) {
        // No configuration required for pure Avro binary decoding
    }

    @Override
    public OrderValue deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            LOGGER.debug("Null/empty data received for deserialization on topic {}", topic);
            return null;
        }
        DatumReader<OrderValue> reader = new SpecificDatumReader<>(OrderValue.class);
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            LOGGER.error("Error deserializing OrderValue from Avro on topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize OrderValue", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}
