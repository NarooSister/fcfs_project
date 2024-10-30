package com.sparta.orderservice.entity;

import com.sparta.orderservice.exception.OrderBusinessException;
import com.sparta.orderservice.exception.OrderServiceErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "ordered_ticket", indexes = {
        @Index(name = "idx_ticket_id", columnList = "ticket_id"),
        @Index(name = "idx_order_id", columnList = "order_id")
})
public class OrderedTicket{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ticketId;
    private Long orderId;
    private Integer quantity;
    private Integer price;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING,   // 주문 처리 중
        CANCELED,   // 취소 완료
        COMPLETED    // 관람 완료
    }

    // 정적 팩토리 메서드
    public static OrderedTicket createPending(Long orderId, Long ticketId, Integer quantity, Integer price) {
        OrderedTicket orderedTicket = new OrderedTicket();
        orderedTicket.orderId = orderId;
        orderedTicket.ticketId = ticketId;
        orderedTicket.quantity = quantity;
        orderedTicket.price = price;
        orderedTicket.status = Status.PENDING;  // 주문 초기 상태 설정
        return orderedTicket;
    }

    public void cancel() {
        if (this.status != Status.PENDING) {
            throw new OrderBusinessException(OrderServiceErrorCode.CANNOT_CANCEL_ORDER);
        }
        this.status = Status.CANCELED;
    }
}
