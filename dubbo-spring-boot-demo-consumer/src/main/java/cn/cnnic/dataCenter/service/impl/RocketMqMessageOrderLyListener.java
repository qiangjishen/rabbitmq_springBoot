package cn.cnnic.dataCenter.service.impl;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.*;


@Component
public class RocketMqMessageOrderLyListener implements MessageListenerOrderly, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(RocketMqMessageOrderLyListener.class);

    private ApplicationContext context;

    @Value("#{'${rocketmq.consumer.orderly.topics:DEMO_ORDERLY_TOPIC}'.split(',')}")
    private List<String> topicList;

    //private Table<String, String, List<MessageHandler>> messageHandlerTable = HashBasedTable.create();

    /**
    @PostConstruct
    public void init() {
        Map<String, MessageHandler> consumers = context.getBeansOfType(MessageHandler.class);
        consumers.values().forEach(
                messageHandler -> {
                    String topic = messageHandler.topic();
                    for (String tagName : messageHandler.tags()) {
                        List<MessageHandler> messageHandlers = messageHandlerTable.get(topic, tagName);
                        if (messageHandlers == null) {
                            messageHandlers = new ArrayList<>(3);
                        }
                        messageHandlers.add(messageHandler);
                        messageHandlerTable.put(topic, tagName, messageHandlers);
                    }
                }
        );
    }


    private List<MessageHandler> getHandler(String topic, String tag) {
        if (StringUtils.isBlank(topic) || StringUtils.isBlank(tag)) {
            return Collections.emptyList();
        }

        return messageHandlerTable.get(topic, tag);
    }

    private void handlerMessageExt(MessageExt messageExt) {
        String topic = messageExt.getTopic();
        String tag = messageExt.getTags();
        String key = messageExt.getKeys();

        // 若传入key，则做唯一性校验
        if (Objects.nonNull(key) && checkMessageKey(key)) {

        }

        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        logger.info("handlerMessageExt,topic:{},tag:{},body:{}", topic, tag, body);
        if (!topicList.contains(topic)) {
            return;
        }

        List<MessageHandler> messageHandlerList = getHandler(topic, tag);
        if (CollectionUtils.isNotEmpty(messageHandlerList)) {
            messageHandlerList.forEach(
                    messageHandler -> {
                        messageHandler.handle(body);
                    }
            );
        }

    }
     **/
    /**
     * 判断是否重复消费
     */
    private boolean checkMessageKey(String key) {
        // TODO: 2021/9/4 可根据redis、数据库保证消息不重复消费
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
        try {
            for (MessageExt messageExt : list) {
               // handlerMessageExt(messageExt);
                logger.info("ThreadName:{},messageExt:{}，消费成功", Thread.currentThread().getName(), messageExt);
            }
            return ConsumeOrderlyStatus.SUCCESS;
        } catch (Exception e) {
            logger.warn("消息消费异常：e:{}", e);
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
    }


}

