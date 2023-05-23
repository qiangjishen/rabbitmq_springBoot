package cn.cnnic.dataCenter.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
@Slf4j
public class RocketMQConsumer3 {


    @Value("${rocketmq.namesrvAddr}")
    private String namesrvAddr;
    @Value("${rocketmq.consumerGroup}")
    private String consumerGroup;
    @Value("${rocketmq.topic}")
    private String topic;
    @Value("${rocketmq.topicTag}")
    private String topicTag;
    @Value("${rocketmq.consumeFromWhere}")
    private String consumeFromWhere;
    @Value("${rocketmq.messageModel}")
    private String messageModel;

    @Bean
    public DefaultMQPushConsumer getRocketMQConsumer() throws MQClientException {
        log.info("======rocketmq  start======");
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(consumerGroup);
        defaultMQPushConsumer.setNamesrvAddr(namesrvAddr);
        defaultMQPushConsumer.setInstanceName(String.valueOf(System.currentTimeMillis()));
        if ("CONSUME_FROM_FIRST_OFFSET".equals(consumeFromWhere)) { //从头到尾消费
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        } else if ("CONSUME_FROM_LAST_OFFSET".equals(consumeFromWhere)) { //从尾部开始消费
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        } else if ("CONSUME_FROM_TIMESTAMP".equals(consumeFromWhere)) { //指定时间消费
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
        } else {
            defaultMQPushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        }
        // 订阅主题和 标签（ * 代表所有标签)下信息
        defaultMQPushConsumer.subscribe(topic, topicTag);
        if (messageModel == null) {
            defaultMQPushConsumer.setMessageModel(MessageModel.CLUSTERING);
        } else if ("CLUSTERING".equals(messageModel)) {  //集群模式
            defaultMQPushConsumer.setMessageModel(MessageModel.CLUSTERING);
        } else if ("BROADCASTING".equals(messageModel)) {  //广播
            defaultMQPushConsumer.setMessageModel(MessageModel.BROADCASTING);
        } else {
            defaultMQPushConsumer.setMessageModel(MessageModel.CLUSTERING);
        }
        //注册消费的监听 并在此监听中消费信息，并返回消费的状态信息
        defaultMQPushConsumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            // msgs中只收集同一个topic，同一个tag，并且key相同的message
            // 会把不同的消息分别放置到不同的队列中
            try {
                for (Message msg : msgs) {
                    // 消费者获取消息 这里只输出 不做后面逻辑处理
                    String body = new String(msg.getBody(), "utf-8");
                    log.info("==========mq参数==========" + body);

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        defaultMQPushConsumer.start();
        log.info("consumerGroup:" + consumerGroup + " namesrvAddr:" + namesrvAddr + "  start success!");
        return defaultMQPushConsumer;
    }
}