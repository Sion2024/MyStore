package com.softuni.finalexam.models.entity;


import com.softuni.finalexam.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private OffsetDateTime date;

    @Column
    private int total;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
