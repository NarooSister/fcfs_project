package com.sparta.fcfsproject.order.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Ordered_ticket", indexes = {
        @Index(name = "idx_ticket_id", columnList = "ticket_id"),
        @Index(name = "idx_order_id", columnList = "order_id")
})
public class OrderedTicket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ticketId;
    private Long orderId;
    private Integer quantity;
    private Integer price;
}
