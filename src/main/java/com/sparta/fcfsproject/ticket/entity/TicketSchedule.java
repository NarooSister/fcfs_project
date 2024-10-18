package com.sparta.fcfsproject.ticket.entity;

import com.sparta.fcfsproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
public class TicketSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ticketId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer stock;

}
