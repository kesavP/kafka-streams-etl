package com.tribune.demo.ecommerce.utils;

import com.tribune.demo.ecommerce.domain.avro.order.OrderValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderAvroSerdeTest {

    @Test
    public void roundTripBinaryAvro() {
        OrderValue original = OrderValue.newBuilder()
                .setId(123L)
                .setTitle("test-title")
                .setPrice(45.67)
                .build();

        OrderAvroSerializer serializer = new OrderAvroSerializer();
        OrderAvroDeserializer deserializer = new OrderAvroDeserializer();

        serializer.configure(null, false);
        deserializer.configure(null, false);

        byte[] bytes = serializer.serialize("test-topic", original);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        OrderValue decoded = deserializer.deserialize("test-topic", bytes);
        assertNotNull(decoded);

        assertEquals(original.getId(), decoded.getId());
        assertEquals(original.getTitle().toString(), decoded.getTitle().toString());
        assertEquals(original.getPrice(), decoded.getPrice(), 1e-6);
    }
}
