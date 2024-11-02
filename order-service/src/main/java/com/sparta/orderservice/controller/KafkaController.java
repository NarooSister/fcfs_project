package com.sparta.orderservice.controller;

import com.sparta.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaController {
    private final OrderService orderService;
//    @GetMapping("/send")
//    public String sendMessage(@RequestParam("topic") String topic,
//                              @RequestParam("key") String key,
//                              @RequestParam("message") String message) {
//        orderService.sendMessage(topic, key, message);
//        return "Message sent to Kafka topic";
//    }

}
