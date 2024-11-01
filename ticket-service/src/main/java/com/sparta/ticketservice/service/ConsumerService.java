package com.sparta.ticketservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsumerService {
    // 이 메서드는 Kafka에서 메시지를 소비하는 리스너 메서드입니다.
    // @KafkaListener 어노테이션은 이 메서드를 Kafka 리스너로 설정합니다.
    @KafkaListener(groupId = "group_a", topics = "topic1")
    // Kafka 토픽 "test-topic"에서 메시지를 수신하면 이 메서드가 호출됩니다.
    // groupId는 컨슈머 그룹을 지정하여 동일한 그룹에 속한 다른 컨슈머와 메시지를 분배받습니다.
    public void consumeFromGroupA(String message) {
        log.info("Group A consumed message from topic1: " + message);
    }

    // 동일한 토픽을 다른 그룹 ID로 소비하는 또 다른 리스너 메서드입니다.
    @KafkaListener(groupId = "group_b", topics = "topic1")
    public void consumeFromGroupB(String message) {
        log.info("Group B consumed message from topic1: " + message);
    }

    // 다른 토픽을 다른 그룹 ID로 소비하는 리스너 메서드입니다.
    @KafkaListener(groupId = "group_c", topics = "topic2")
    public void consumeFromTopicC(String message) {
        log.info("Group C consumed message from topic2: " + message);
    }

    // 다른 토픽을 다른 그룹 ID로 소비하는 리스너 메서드입니다.
    @KafkaListener(groupId = "group_c", topics = "topic3")
    public void consumeFromTopicD(String message) {
        log.info("Group C consumed message from topic3: " + message);
    }

    @KafkaListener(groupId = "group_d", topics = "topic4")
    public void consumeFromPartition0(String message) {
        log.info("Group D consumed message from topic4: " + message);
    }
}
