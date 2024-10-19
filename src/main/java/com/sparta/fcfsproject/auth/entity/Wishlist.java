package com.sparta.fcfsproject.auth.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Wishlist", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_ticket_id", columnList = "ticket_id")
})
public class Wishlist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long ticketId;

    private Integer quantity;

}
