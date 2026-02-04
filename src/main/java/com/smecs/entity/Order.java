package com.smecs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double totalAmount;

    public enum Status {
        PENDING, SHIPPED, DELIVERED, CANCELLED
    }

    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDateTime createdAt;
}
