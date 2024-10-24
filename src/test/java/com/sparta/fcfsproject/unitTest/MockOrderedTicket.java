package com.sparta.fcfsproject.unitTest;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import com.sparta.fcfsproject.order.entity.OrderedTicket;
import org.springframework.core.Ordered;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class MockOrderedTicket extends OrderedTicket {
    // 생성자 추가
    public MockOrderedTicket(Long id, Long orderId, Long ticketId, Integer quantity, Integer price) {
        super(); // 부모 클래스의 기본 생성자 호출
        // OrderedTicket의 필드에 접근할 수 없기 때문에 Reflection을 통해 설정할 수 있습니다.
        try {
            Field idField = OrderedTicket.class.getDeclaredField("id");
            Field orderIdField = OrderedTicket.class.getDeclaredField("orderId");
            Field ticketIdField = OrderedTicket.class.getDeclaredField("ticketId");
            Field quantityField = OrderedTicket.class.getDeclaredField("quantity");
            Field priceField = OrderedTicket.class.getDeclaredField("price");
            Field statusField = OrderedTicket.class.getDeclaredField("status");

            idField.setAccessible(true);
            orderIdField.setAccessible(true);
            ticketIdField.setAccessible(true);
            quantityField.setAccessible(true);
            priceField.setAccessible(true);
            statusField.setAccessible(true);

            idField.set(this, id); // id 설정
            orderIdField.set(this, orderId);
            ticketIdField.set(this, ticketId);
            quantityField.set(this, quantity);
            priceField.set(this, price);
            statusField.set(this, Status.PENDING); // 초기 상태 설정
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // 예외 처리
        }
    }

    // createdAt 필드를 설정하기 위한 메서드
    public void setCreatedAt(LocalDateTime createdAt) {
        try {
            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(this, createdAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // 예외 처리
        }
    }
}