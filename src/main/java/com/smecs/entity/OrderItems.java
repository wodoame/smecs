package com.smecs.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "OrderItems")
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    // Getters and setters
    // ...
}
