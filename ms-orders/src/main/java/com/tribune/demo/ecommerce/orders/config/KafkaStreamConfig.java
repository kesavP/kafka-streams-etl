package com.tribune.demo.ecommerce.orders.config;


import com.tribune.demo.ecommerce.domain.Topics;
import com.tribune.demo.ecommerce.domain.avro.order.OrderKey;
import com.tribune.demo.ecommerce.domain.avro.order.OrderValue;
import com.tribune.demo.ecommerce.orders.service.OrderService;
import com.tribune.demo.ecommerce.utils.OrderAvroValueSerde;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.tribune.demo.ecommerce.domain.Topics.ORDERS;
import static org.apache.kafka.streams.StreamsConfig.*;


@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableKafkaStreams
public class KafkaStreamConfig {

    private final OrderService orderService;

    @Bean(name = "defaultKafkaStreamsConfig")
    public KafkaStreamsConfiguration kStreamsConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        log.info("Configuring Kafka Streams");
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "ms-orders-streams-app");
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.LongSerde.class);
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, OrderAvroValueSerde.class);
        props.put(NUM_STREAM_THREADS_CONFIG, 3);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8000");

        return new KafkaStreamsConfiguration(props);
    }

    //this is the one that will get results from other topics to check
    @Bean
    public KStream<Long, OrderValue> stream(StreamsBuilder builder) {
        Map<String, Object> serdeConfig = new HashMap<>();
//        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8000");
//        serdeConfig.put("specific.avro.reader", true);

        Serde<Long> keySerde = Serdes.Long();
        Serde<OrderValue> valueSerde = new OrderAvroValueSerde();
//        keySerde.configure(serdeConfig, true);
//        valueSerde.configure(serdeConfig, false);

        KStream<Long, OrderValue> paymentStream = builder
                .stream(Topics.PAYMENTS, Consumed.with(keySerde, valueSerde))
                .peek((key, value) -> log.info("payments topic payload {}", String.valueOf(value)));
        KStream<Long, OrderValue> stockStream = builder
                .stream(Topics.STOCK, Consumed.with(keySerde, valueSerde))
                .peek((key, value) -> log.info("stock topic payload {}", String.valueOf(value)));

        KStream<Long, OrderValue> joinedStream = paymentStream.join(
                        stockStream,
                        (payment, stock) -> payment,
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(10)),
                        StreamJoined.with(keySerde, valueSerde, valueSerde)
                )
                .peek((k, v) -> log.info("Kafka stream match: key[{}],value[{}]", k, v));

        joinedStream.to(ORDERS, Produced.with(keySerde, valueSerde));
        return joinedStream;
    }

    /**
     * To build a persistent key-value store
     * This KTable will be used to store all the Orders
     ***/

//     @Bean
//     public KTable<Long, Order> table(StreamsBuilder builder) {

//         KeyValueBytesStoreSupplier store = Stores.persistentKeyValueStore(KafkaIds.ORDERS);

//         Serde<Long> keySerde = Serdes.Long();
//         Serde<Order> valueSerde = new OrderJsonSerde();

//         KStream<Long, Order> stream = builder
//                 .stream(ORDERS, Consumed.with(keySerde, valueSerde))
//                 .peek((k, v) -> log.info("Kafka persistence table: key[{}],value[{}]", k, v));

//         return stream.toTable(Materialized.<Long, Order>as(store)
//                 .withKeySerde(keySerde)
//                 .withValueSerde(valueSerde));
//     }
}
