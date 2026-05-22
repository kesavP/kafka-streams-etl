package com.tribune.demo.ecommerce.orders.service;


import com.tribune.demo.ecommerce.domain.*;
import com.tribune.demo.ecommerce.domain.avro.order.OrderKey;
import com.tribune.demo.ecommerce.domain.avro.order.OrderValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public record OrderServiceImpl(KafkaTemplate<Long, OrderValue> kafkaTemplate) implements OrderService {


    @Override
    public Order confirm(Order orderPayment, Order orderStock) {

        Order o = Order.builder()
                .id(orderPayment.getId())
                .customerId(orderPayment.getCustomerId())
                .productId(orderPayment.getProductId())
                .productCount(orderPayment.getProductCount())
                .price(orderPayment.getPrice())
                .build();

        if (orderPayment.getStatus().equals(OrderStatus.ACCEPT) &&
                orderStock.getStatus().equals(OrderStatus.ACCEPT)) {
            log.info("Confirming order {}", orderPayment.getId());
            o.setStatus(OrderStatus.CONFIRMED);
        } else if (orderPayment.getStatus().equals(OrderStatus.REJECT) &&
                orderStock.getStatus().equals(OrderStatus.REJECT)) {
            log.info("Rejecting order {}", orderPayment.getId());
            o.setStatus(OrderStatus.REJECTED);
        } else if (orderPayment.getStatus().equals(OrderStatus.REJECT) ||
                orderStock.getStatus().equals(OrderStatus.REJECT)) {
            OrderSource source = orderPayment.getStatus().equals(OrderStatus.REJECT)
                    ? OrderSource.PAYMENT : OrderSource.STOCK;
            log.info("Rollback order {}", orderPayment.getId());
            o.setStatus(OrderStatus.ROLLBACK);
            o.setSource(source);//source of error
        }
        return o;
    }

    @Override
    public OrderAvro createOrder(OrderAvro order) {
        try {
            // Generate UUID as order ID (better than AtomicLong for distributed systems)
            long orderId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
            order.setId(orderId);
//            order.setStatus(OrderStatus.NEW);

            log.info("Creating order with ID: {} for customer: {}", orderId, order.getTitle());
            OrderKey key = OrderKey.newBuilder().setId(orderId).build();
            OrderValue value = OrderValue.newBuilder().setId(orderId).setTitle(order.getTitle()).setPrice(1000f).build();

            final ProducerRecord<Long, OrderValue> producerRecord =
                    new ProducerRecord<>(Topics.ORDERS, orderId, value);

            // Send to Kafka with timeout
            kafkaTemplate.send(producerRecord)
                    .get(10, TimeUnit.SECONDS);  // Add timeout to prevent hanging

            log.info("Order successfully sent to Kafka: {}", order);
            return order;

        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage(), e);
            throw new com.tribune.demo.ecommerce.orders.error.OrderProcessingException(
                    "Failed to process order: " + e.getMessage(), e);
        }
    }

}
