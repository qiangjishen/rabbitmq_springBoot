package cn.cnnic.data.center.service.impl;


import cn.cnnic.data.center.config.LazyRabbitConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LazyConsume1 {
    /**
     * 惰性队列，主要用于处理数据积压，消息持久化于硬盘，用的时候才加载到内存
     *
     * @param channel 信道
     * @param message 消息
     * @throws Exception
     */
    @RabbitListener(queues = LazyRabbitConfig.LAZY_TOPIC_QUEUE, ackMode = "MANUAL")
    public void onMessage(Channel channel, Message message) throws Exception {
        log.info("--------------------------------------");
        log.info("消费端Payload: " + new String(message.getBody())+"-ID:"+message.getMessageProperties().getAppId()+"-messageId:"+message.getMessageProperties().getMessageId());
        Long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //手工ACK,获取deliveryTag
        channel.basicAck(deliveryTag, false);
    }

}
