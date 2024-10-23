package com.sparta.fcfsproject.order.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
@Setter
public class CartItem implements Serializable {
    private Long ticketId;
    private String ticketName;
    private int quantity;
    private int price;

    public CartItem(Long ticketId, String ticketName, int quantity, int price) {
        this.ticketId = ticketId;
        this.ticketName = ticketName;
        this.quantity = quantity;
        this.price = price;
    }
}
