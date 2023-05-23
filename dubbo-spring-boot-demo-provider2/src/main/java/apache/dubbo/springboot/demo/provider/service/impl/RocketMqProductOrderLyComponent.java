package apache.dubbo.springboot.demo.provider.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class RocketMqProductOrderLyComponent {

    private Logger logger = LoggerFactory.getLogger(RocketMqProductOrderLyComponent.class);

    @Resource
    private DefaultMQProducer defaultMQProducer;

    /**
     * 发送消息
     *
     * @param dto       具体数据
     * @param topicName
     * @param tagName
     * @return 执行状态
     */
    public boolean sendMessage(Object dto, String topicName, String tagName, String key) {
        if (Objects.isNull(dto) || Objects.isNull(topicName) || Objects.isNull(tagName) || Objects.isNull(key)) {
            return false;
        }

        boolean result = false;

        // 构造消息body
        String body = builderBody(dto);

        try {
            Message message = new Message(topicName, tagName, key, body.getBytes(StandardCharsets.UTF_8));
            /**
             * 局部的顺序消息
             * message:消息信息
             * arg：选择队列的业务标识
             */
            SendResult send = defaultMQProducer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> list, Message message, Object o) {
                    Long key = Long.parseLong((String) o);
                    int index = (int) (key % list.size());
                    return list.get(index);
                }
            }, key);

            System.err.println("发送者，发送消息：" + JSON.toJSONString(send));
            if (Objects.nonNull(send) && SendStatus.SEND_OK.equals(send.getSendStatus())) {
                result = true;
            } else {
                logger.warn("消息发送失败,send={},body={}", JSON.toJSONString(send), body);
            }
        } catch (MQClientException | RemotingException | MQBrokerException e) {
            logger.warn("发送消息失败：｛｝", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * 发送延时消息
     *
     * @param dto
     * @param topicName
     * @param tagName
     * @param delayLevelEnum 延时等级
     * @return
     */
    public boolean sendDelayMessage(Object dto, String topicName, String tagName, String key, DelayLevelEnum delayLevelEnum) {
        if (Objects.isNull(dto) || Objects.isNull(topicName) || Objects.isNull(tagName) || Objects.isNull(key) || Objects.isNull(delayLevelEnum)) {
            return false;
        }

        boolean result = false;

        // 构造消息body
        String body = builderBody(dto);
        try {
            Message message = new Message(topicName, tagName, key, body.getBytes(StandardCharsets.UTF_8));
            message.setDelayTimeLevel(delayLevelEnum.getDelayLevel());

            SendResult send = defaultMQProducer.send(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> list, Message message, Object o) {
                    Long key = (Long) o;
                    int index = (int) (key % list.size());
                    return list.get(index);
                }
            }, key);
            if (Objects.nonNull(send) && SendStatus.SEND_OK.equals(send.getSendStatus())) {
                result = true;
            } else {
                logger.warn("延时消息发送失败,send={},body={}", JSON.toJSONString(send), body);
            }
        } catch (MQClientException | RemotingException | MQBrokerException e) {
            logger.warn("发送延时消息失败：｛｝", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * 构造消息body
     *
     * @param dto
     * @return
     */
    public String builderBody(Object dto) {
        // 构造消息body
        String body = null;
        if (dto instanceof String) {
            body = (String) dto;
        } else {
            body = JSON.toJSONString(dto);
        }
        return body;
    }
}
