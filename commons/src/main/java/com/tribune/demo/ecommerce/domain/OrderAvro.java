package com.tribune.demo.ecommerce.domain;

import lombok.*;
import org.apache.avro.LogicalTypes;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAvro {

    private Long id;

    private String title;

    private double price;


    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", title=" + title +
                ", price=" + price +
                '}';
    }
}
