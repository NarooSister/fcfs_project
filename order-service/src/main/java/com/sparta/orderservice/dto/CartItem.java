package com.sparta.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
@NoArgsConstructor
public class CartItem implements Serializable {
    @NotNull(message = "ticketId는 필수 입력값입니다.")
    private Long ticketId;

    @NotNull(message = "ticketName은 필수 입력값입니다.")
    private String ticketName;

    @NotNull(message = "quantity는 필수 입력값입니다.")
    private int quantity;

    @NotNull(message = "price는 필수 입력값입니다.")
    private int price;

    public CartItem(Long ticketId, String ticketName, int quantity, int price) {
        this.ticketId = ticketId;
        this.ticketName = ticketName;
        this.quantity = quantity;
        this.price = price;
    }
}
