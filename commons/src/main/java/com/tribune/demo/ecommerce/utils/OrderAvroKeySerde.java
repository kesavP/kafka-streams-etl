package com.tribune.demo.ecommerce.utils;

import com.tribune.demo.ecommerce.domain.avro.order.OrderKey;
import com.tribune.demo.ecommerce.domain.avro.order.OrderValue;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class OrderAvroKeySerde implements Serde<OrderKey> {
    @Override
    public Serializer<OrderKey> serializer() {
        return null;
    }

    @Override
    public Deserializer<OrderKey> deserializer() {
        return null;
    }
}
