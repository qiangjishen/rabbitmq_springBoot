package cn.cnnic.data.center.controller;

import cn.cnnic.data.center.dto.MessageBody;
import cn.cnnic.data.center.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/rocket")
@Slf4j
public class RocketController {

    private static enum MSG_TYPE{ ONEWAY, ASYNC, SYNC };

    @Autowired
    public RocketMQTemplate rocketMQTemplate;

    /**
     * 发送消息，通用
     * @param msg_type
     * @param destination
     * @param payload
     */
    @RequestMapping(value = "/send",method = RequestMethod.GET)
    private void sendMsg(MSG_TYPE msg_type, String destination, Object payload, String msgSource){
        String msgKey = String.valueOf(SnowFlake.getId());
        MessageBody msgBody = new MessageBody(msgKey, payload , msgSource);
        Message<MessageBody> message = MessageBuilder.withPayload(msgBody).setHeader("KEYS",msgKey ).build();
        log.info(String.format("消息发送 MQService 开始: %s %s", destination, message));
        SendResult result = null;
        switch (msg_type) {
            case ONEWAY:

                rocketMQTemplate.sendOneWay(destination, message);
                break;
            case ASYNC:
                rocketMQTemplate.asyncSend(destination, message,new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("发送成功------------------------");
                    }
                    @Override
                    public void onException(Throwable throwable) {
                        log.error("MQService:" + ExceptionUtils.getStackTrace(throwable));
                       // throw new CustomException(String.format("消息发送失败 topic_tag:%s", destination ));
                    }
                });
                break;
            case SYNC:
                result = rocketMQTemplate.syncSend(destination, message);
                break;
        }
        log.info(String.format("消息发送 MQService 结束: msgId: %s dest: %s msg: %s",result != null ? result.getMsgId() : "", destination, message));
    }

    /**
     * 同步发送消息,会确认应答
     * @param destination
     * @param payload
     */
    public void syncSendMsg(String destination, Object payload, String msgSource){
        sendMsg(MSG_TYPE.SYNC,destination, payload,msgSource) ;
    }
    /**
     * 同步发送消息,会确认应答
     * @param topic
     * @param tag
     * @param payload
     */
    public void syncSendMsg(String topic, String tag, Object payload, String msgSource){
        // 发送的消息体，消息体必须存在
        // 业务主键作为消息key
        String destination = topic + ":" + tag;
        syncSendMsg(destination, payload,msgSource);
    }

    /**
     * 异步消息发送,异步日志确认异常
     * @param destination
     * @param payload
     */
    public void asyncSendMsg(String destination, Object payload, String msgSource){
        sendMsg(MSG_TYPE.ASYNC,destination, payload,msgSource);
    }
    /**
     * 异步消息发送,异步日志确认异常
     * @param topic
     * @param tag
     * @param payload
     * @return
     */
    public void asyncSendMsg(String topic, String tag, Object payload, String msgSource){
        // 发送的消息体，消息体必须存在
        // 业务主键作为消息key
        String destination = topic + ":" + tag;
        asyncSendMsg(destination, payload,msgSource);
    }

    /**
     * 单向发送消息，不关注结果
     * @param destination
     * @param payload
     */
    public void oneWaySendMsg(String destination, Object payload, String msgSource){
        sendMsg(MSG_TYPE.ONEWAY,destination, payload,msgSource);
    }
    /**
     * 单向发送消息，不关注结果
     * @param topic
     * @param tag
     * @param payload
     */
    public void oneWaySendMsg(String topic, String tag, Object payload, String msgSource){
        // 发送的消息体，消息体必须存在
        // 业务主键作为消息key
        String destination = topic + ":" + tag;
        oneWaySendMsg(destination, payload,msgSource);
    }
}

