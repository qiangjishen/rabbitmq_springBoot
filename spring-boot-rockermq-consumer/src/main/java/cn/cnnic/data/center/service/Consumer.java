package cn.cnnic.data.center.service;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "sdnsd-topic",consumerGroup = "my-consumer-group",consumeMode = ConsumeMode.CONCURRENTLY, messageModel = MessageModel.CLUSTERING,  selectorExpression = "*")
public class Consumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        System.out.println("消费者收到信息：==="+message);
    }
}
