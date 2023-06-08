package cn.cnnic.data.center.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/*
 * 【生产者】模拟用户发起转账请求
 */
@Slf4j
@Service
@Component
public class TransactionProducerService {
    // TOPIC名称
    private static final String TOPIC = "transTopic";
    // TAG信息
    private static final String TAG = "transTag";

    private static final String Tx_Charge_Group = "Tx_Charge_Group";


    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public TransactionSendResult sendHalfMsg(String msg) {
        // 生成事务ID
        String transactionId = UUID.randomUUID().toString().replace("-", "");
        log.info("【发送半消息】transactionId={}", transactionId);
        String transKeys = "transKey";

        // 发送事务消息
        // 发送事务消息（参1：生产者所在事务组，参2：topic+tag，参3：消息体(可以传参)，参4：发送参数）
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(
                TOPIC + ":" + TAG,
                MessageBuilder.withPayload(msg)
                        .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                        .setHeader(RocketMQHeaders.KEYS, transKeys)     // 相比于使用"KEYS"，使用封装常量更不易出错
                        .build(),
                msg
        );
        log.info("【发送半消息】sendResult={}", msg);
        return sendResult;
    }







}

