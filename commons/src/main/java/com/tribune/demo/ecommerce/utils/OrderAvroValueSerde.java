package com.tribune.demo.ecommerce.utils;

import com.tribune.demo.ecommerce.domain.avro.order.OrderValue;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class OrderAvroValueSerde implements Serde<OrderValue> {
    @Override
    public Serializer<OrderValue> serializer() {
        return new OrderAvroSerializer();
    }

    @Override
    public Deserializer<OrderValue> deserializer() {
        return new OrderAvroDeserializer();
    }
}
